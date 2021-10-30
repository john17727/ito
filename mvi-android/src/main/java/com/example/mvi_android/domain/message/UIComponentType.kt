package com.example.mvi_android.domain.message

sealed class UIComponentType {

    object Toast : UIComponentType()

    object Dialog : UIComponentType()

    object None : UIComponentType()
}
