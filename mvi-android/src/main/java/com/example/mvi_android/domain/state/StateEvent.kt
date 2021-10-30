package com.example.mvi_android.domain.state

interface StateEvent {

    fun errorInfo(): String

    fun eventName(): String

    fun shouldDisplayProgressBar(): Boolean

    fun shouldDisplayMessage(): Boolean
}