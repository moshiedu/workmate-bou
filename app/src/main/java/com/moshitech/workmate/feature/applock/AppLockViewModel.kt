package com.moshitech.workmate.feature.applock

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppLockState {
    SETUP, // Setting up a new PIN
    CONFIRM_SETUP, // Confirming the new PIN
    VERIFY, // Unlocking the app
    CHANGE_VERIFY, // Verifying old PIN before changing
    CHANGE_NEW, // Entering new PIN
    CHANGE_CONFIRM // Confirming new PIN
}

class AppLockViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(AppLockState.SETUP)
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _message = MutableStateFlow("Enter PIN")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var tempPin: String? = null

    init {
        checkPinSet()
    }

    private fun checkPinSet() {
        val savedPin = prefs.getString("pin", null)
        if (savedPin != null) {
            _state.value = AppLockState.VERIFY
            _message.value = "Enter PIN to Unlock"
        } else {
            _state.value = AppLockState.SETUP
            _message.value = "Create a 4-digit PIN"
        }
    }

    fun onPinDigit(digit: String) {
        if (_pin.value.length < 4) {
            _pin.value += digit
            _error.value = null
            if (_pin.value.length == 4) {
                processPin(_pin.value)
            }
        }
    }

    fun onDeleteDigit() {
        if (_pin.value.isNotEmpty()) {
            _pin.value = _pin.value.dropLast(1)
            _error.value = null
        }
    }

    private fun processPin(enteredPin: String) {
        when (_state.value) {
            AppLockState.SETUP -> {
                tempPin = enteredPin
                _state.value = AppLockState.CONFIRM_SETUP
                _pin.value = ""
                _message.value = "Confirm PIN"
            }
            AppLockState.CONFIRM_SETUP -> {
                if (enteredPin == tempPin) {
                    savePin(enteredPin)
                    _state.value = AppLockState.VERIFY
                    _message.value = "PIN Set Successfully"
                    // Navigate back or to home handled by UI event
                } else {
                    _error.value = "PINs do not match"
                    _pin.value = ""
                    tempPin = null
                    _state.value = AppLockState.SETUP
                    _message.value = "Create a 4-digit PIN"
                }
            }
            AppLockState.VERIFY -> {
                val savedPin = prefs.getString("pin", null)
                if (enteredPin == savedPin) {
                    // Success - UI handles navigation
                    _message.value = "Unlocked"
                } else {
                    _error.value = "Incorrect PIN"
                    _pin.value = ""
                }
            }
            // Add logic for changing PIN if needed later
            else -> {}
        }
    }

    private fun savePin(pin: String) {
        prefs.edit().putString("pin", pin).apply()
    }
    
    fun resetState() {
        _pin.value = ""
        _error.value = null
        checkPinSet()
    }
}
