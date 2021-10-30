package com.example.mvi_android.domain.message

sealed class MessageType {

    object Success : MessageType()

    object Error : MessageType()

    object Info : MessageType()

    object None : MessageType()
}
