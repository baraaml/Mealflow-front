package com.example.mealflow.ui.components

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

object AppState {
    var isFreshStart = true

    fun setup() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                isFreshStart = true
            } else if (event == Lifecycle.Event.ON_STOP) {
                isFreshStart = false
            }
        })
    }
}
