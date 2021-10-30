package com.example.mvi_android.domain.message

data class StateMessage(
    val message: String?,
    val uiComponentType: UIComponentType,
    val messageType: MessageType
)
