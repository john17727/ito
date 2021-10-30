package com.example.mvi_android.domain.state

import com.example.mvi_android.domain.message.StateMessage

data class DataState<T>(
    var stateMessage: StateMessage? = null,
    var data: T? = null,
    var stateEvent: StateEvent? = null
) {

    companion object {
        fun <T> error(
            message: StateMessage,
            stateEvent: StateEvent?
        ): DataState<T> {
            return DataState(
                stateMessage = message,
                data = null,
                stateEvent = stateEvent
            )
        }

        fun <T> data(
            message: StateMessage?,
            data: T? = null,
            stateEvent: StateEvent?
        ): DataState<T> {
            return DataState(
                stateMessage = message,
                data = data,
                stateEvent = stateEvent
            )
        }
    }
}
