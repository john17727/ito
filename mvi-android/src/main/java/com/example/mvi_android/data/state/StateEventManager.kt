package com.example.mvi_android.data.state

import com.example.mvi_android.domain.state.StateEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StateEventManager {

    private val activeStateEvents: HashMap<String, StateEvent> = HashMap()

    private val _shouldDisplayProgressBar: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val shouldDisplayProgressBar = _shouldDisplayProgressBar.asStateFlow()

    fun getActiveJobNames(): MutableSet<String>{
        return activeStateEvents.keys
    }

    fun clearActiveStateEventCounter(){
        activeStateEvents.clear()
        syncNumActiveStateEvents()
    }

    fun addStateEvent(stateEvent: StateEvent){
        activeStateEvents.put(stateEvent.eventName(), stateEvent)
        syncNumActiveStateEvents()
    }

    fun removeStateEvent(stateEvent: StateEvent?){
        activeStateEvents.remove(stateEvent?.eventName())
        syncNumActiveStateEvents()
    }

    fun isStateEventActive(stateEvent: StateEvent): Boolean{
        for(eventName in activeStateEvents.keys){
            if(stateEvent.eventName() == eventName){
                return true
            }
        }
        return false
    }

    private fun syncNumActiveStateEvents(){
        var shouldDisplayProgressBar = false
        for(stateEvent in activeStateEvents.values){
            if(stateEvent.shouldDisplayProgressBar()){
                shouldDisplayProgressBar = true
            }
        }
        _shouldDisplayProgressBar.value = shouldDisplayProgressBar
    }
}