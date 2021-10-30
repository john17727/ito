package com.example.mvi_android.presentation

import androidx.lifecycle.ViewModel
import com.example.mvi_android.data.DataChannelManager
import com.example.mvi_android.domain.message.MessageType
import com.example.mvi_android.domain.message.StateMessage
import com.example.mvi_android.domain.message.UIComponentType
import com.example.mvi_android.domain.state.DataState
import com.example.mvi_android.domain.state.StateEvent
import com.example.mvi_android.domain.state.ViewState
import com.example.mvi_android.domain.util.GenericErrors.INVALID_STATE_EVENT
import kotlinx.coroutines.flow.*

abstract class StateViewModel<UiState : ViewState> : ViewModel() {

    // Creates a manager that reduces state events to UI states from a flow
    private val dataChannelManager: DataChannelManager<UiState> =
        object : DataChannelManager<UiState>() {
            override fun handleNewData(data: UiState) {
                handleNewState(data)
            }
        }

    // Holds the initial state of the UI
    private val initialState: UiState by lazy { setInitialState() }

    // Holds the current state of the UI
    private val _viewState: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    val viewState: StateFlow<UiState> = _viewState.asStateFlow()

    // Holds the loading status of an event
    val isLoading: StateFlow<Boolean> = dataChannelManager.shouldDisplayProgressBar

    // Holds the latest message
    val message = dataChannelManager.messageStack.stateMessage

    // Sets the initial state of the UI when the ViewModel is first created
    abstract fun setInitialState(): UiState

    // Returns the flow to execute based on the StateEvent type
    abstract fun handleEvents(event: StateEvent): Flow<DataState<UiState>>

    // Launches an event
    fun startEvent(event: StateEvent) {
        val job = handleEvents(event)
        launchJob(event, job)
    }

    // Sets the new UI State
    protected fun setState(reducer: UiState.() -> UiState) {
        val newState = viewState.value.reducer()
        _viewState.value = newState
    }

    // Overridable function to handle an incoming state
    open fun handleNewState(data: UiState) {
        setState { data }
    }

    // Returns an empty state event. Used to emit an invalid message for state events not found.
    fun emitInvalidStateEvent(stateEvent: StateEvent) = flow {
        emit(
            DataState.error<UiState>(
                message = StateMessage(
                    message = INVALID_STATE_EVENT,
                    uiComponentType = UIComponentType.None,
                    messageType = MessageType.Error
                ),
                stateEvent = stateEvent
            )
        )
    }

    // Removes the latest message
    fun removeMessage() = dataChannelManager.clearStateMessage()

    // Clears all messages
    fun removeAllMessages() = dataChannelManager.clearAllStateMessages()

    // Launches the job responsible for a state event
    private fun launchJob(stateEvent: StateEvent, jobFunction: Flow<DataState<UiState>?>) {
        dataChannelManager.launchJob(stateEvent, jobFunction)
    }

}