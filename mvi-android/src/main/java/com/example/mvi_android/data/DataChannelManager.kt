package com.example.mvi_android.data

import com.example.mvi_android.data.message.MessageStack
import com.example.mvi_android.data.state.StateEventManager
import com.example.mvi_android.domain.message.MessageType
import com.example.mvi_android.domain.message.StateMessage
import com.example.mvi_android.domain.state.DataState
import com.example.mvi_android.domain.state.StateEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class DataChannelManager<ViewState> {

    private var channelScope: CoroutineScope? = null
    private val stateEventManager: StateEventManager = StateEventManager()

    val messageStack = MessageStack(getChannelScope())

    val shouldDisplayProgressBar = stateEventManager.shouldDisplayProgressBar

    abstract fun handleNewData(data: ViewState)

    fun launchJob(stateEvent: StateEvent, jobFunction: Flow<DataState<ViewState>?>) {
        if (canExecuteNewStateEvent(stateEvent)) {
            addStateEvent(stateEvent)
            jobFunction
                .onEach { dataState ->
                    dataState?.let { allData ->
                        withContext(Main) {
                            allData.data?.let { data ->
                                handleNewData(data)
                            }
                            allData.stateMessage?.let { stateMessage ->
                                if (stateEvent.shouldDisplayMessage()) {
                                    handleNewStateMessage(stateMessage)
                                }
                            }
                            allData.stateEvent?.let { stateEvent ->
                                removeStateEvent(stateEvent)
                            }
                        }
                    }
                }
                .launchIn(getChannelScope())
        }
    }

    private fun canExecuteNewStateEvent(stateEvent: StateEvent): Boolean {
        // If a job is already active, do not allow duplication
        if (isJobAlreadyActive(stateEvent)) {
            return false
        }
        // if a dialog is showing, do not allow new StateEvents
        if (!isMessageStackEmpty()) {
            return false
        }
        return true
    }

    private fun isMessageStackEmpty(): Boolean {
        return messageStack.isStackEmpty()
    }

    private fun handleNewStateMessage(stateMessage: StateMessage) {
        appendStateMessage(stateMessage)
    }

    private fun appendStateMessage(stateMessage: StateMessage) {
        messageStack.add(stateMessage)
    }

    fun clearStateMessage(index: Int = 0) {
        messageStack.removeAt(index)
    }

    fun clearAllStateMessages() = messageStack.clear()

    fun printStateMessages() {
        for (message in messageStack) {
            message
        }
    }

    // for debugging
    fun getActiveJobs() = stateEventManager.getActiveJobNames()

    fun clearActiveStateEventCounter() = stateEventManager.clearActiveStateEventCounter()

    fun addStateEvent(stateEvent: StateEvent) = stateEventManager.addStateEvent(stateEvent)

    private fun removeStateEvent(stateEvent: StateEvent?) =
        stateEventManager.removeStateEvent(stateEvent)

    private fun isStateEventActive(stateEvent: StateEvent) =
        stateEventManager.isStateEventActive(stateEvent)

    private fun isJobAlreadyActive(stateEvent: StateEvent): Boolean {
        return isStateEventActive(stateEvent)
    }

    private fun getChannelScope(): CoroutineScope {
        return channelScope ?: setupNewChannelScope(CoroutineScope(Dispatchers.IO))
    }

    private fun setupNewChannelScope(coroutineScope: CoroutineScope): CoroutineScope {
        channelScope = coroutineScope
        return channelScope as CoroutineScope
    }

    fun cancelJobs() {
        if (channelScope != null) {
            if (channelScope?.isActive == true) {
                channelScope?.cancel()
            }
            channelScope = null
        }
        clearActiveStateEventCounter()
    }

}