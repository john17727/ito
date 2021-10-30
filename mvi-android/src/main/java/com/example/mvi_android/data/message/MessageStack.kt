package com.example.mvi_android.data.message

import com.example.mvi_android.domain.message.MessageType
import com.example.mvi_android.domain.message.StateMessage
import com.example.mvi_android.domain.message.UIComponentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.IndexOutOfBoundsException

class MessageStack(private val externalScope: CoroutineScope) : ArrayList<StateMessage>() {

    private val _stateMessage: MutableStateFlow<StateMessage?> = MutableStateFlow(null)

    val stateMessage = _stateMessage.asStateFlow()

    fun isStackEmpty(): Boolean {
        return size == 0
    }

    override fun addAll(elements: Collection<StateMessage>): Boolean {
        for (element in elements) {
            add(element)
        }
        return true // always return true. We don't care about result bool.
    }

    override fun add(element: StateMessage): Boolean {
        if (this.contains(element)) { // prevent duplicate errors added to stack
            return false
        }
        val transaction = super.add(element)
        if (this.size == 1) {
            setStateMessage(stateMessage = element)
        }
        return transaction
    }

    override fun removeAt(index: Int): StateMessage {
        try {
            val transaction = super.removeAt(index)
            if (this.size > 0) {
                setStateMessage(stateMessage = this[0])
            } else {
                setStateMessage(null)
            }
            return transaction
        } catch (e: IndexOutOfBoundsException) {
            setStateMessage(null)
            e.printStackTrace()
        }
        return StateMessage(
            message = "does nothing",
            uiComponentType = UIComponentType.None,
            messageType = MessageType.None
        )
    }

    private fun setStateMessage(stateMessage: StateMessage?) {
        externalScope.launch {
            _stateMessage.emit(stateMessage)
        }
    }
}