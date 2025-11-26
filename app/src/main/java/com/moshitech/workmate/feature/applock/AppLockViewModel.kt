package com.moshitech.workmate.feature.applock

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moshitech.workmate.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class AppLockState {
    SETUP, // Setting up a new PIN
    CONFIRM_SETUP, // Confirming the new PIN
    VERIFY, // Unlocking the app
    DASHBOARD // Main screen with app list
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

class AppLockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    private val _state = MutableStateFlow(AppLockState.DASHBOARD)
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var tempPin: String? = null

    // App List Data
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _lockedApps = MutableStateFlow<Set<String>>(emptySet())
    val lockedApps: StateFlow<Set<String>> = _lockedApps.asStateFlow()

    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    init {
        loadInstalledApps()
        checkPinStatus()
        
        viewModelScope.launch {
            repository.lockedApps.collectLatest {
                _lockedApps.value = it
            }
        }
        viewModelScope.launch {
            repository.isAppLockEnabled.collectLatest {
                _isServiceEnabled.value = it
            }
        }
    }

    private fun checkPinStatus() {
        viewModelScope.launch {
            val savedPin = repository.appLockPin.firstOrNull()
            if (savedPin.isNullOrEmpty()) {
                _state.value = AppLockState.SETUP
                _message.value = "Create a 4-digit PIN"
            } else {
                _state.value = AppLockState.VERIFY
                _message.value = "Enter PIN"
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val apps = pm.getInstalledPackages(PackageManager.GET_META_DATA)
                .filter { 
                    pm.getLaunchIntentForPackage(it.packageName) != null &&
                    it.packageName != getApplication<Application>().packageName 
                }
                .mapNotNull {
                    val appInfo = it.applicationInfo ?: return@mapNotNull null
                    AppInfo(
                        name = appInfo.loadLabel(pm).toString(),
                        packageName = it.packageName,
                        icon = appInfo.loadIcon(pm)
                    )
                }
                .sortedBy { it.name }
            _installedApps.value = apps
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
        viewModelScope.launch {
            when (_state.value) {
                AppLockState.SETUP -> {
                    tempPin = enteredPin
                    _state.value = AppLockState.CONFIRM_SETUP
                    _pin.value = ""
                    _message.value = "Confirm PIN"
                }
                AppLockState.CONFIRM_SETUP -> {
                    if (enteredPin == tempPin) {
                        repository.setAppLockPin(enteredPin)
                        _state.value = AppLockState.DASHBOARD
                        _message.value = "PIN Set Successfully"
                    } else {
                        _error.value = "PINs do not match"
                        _pin.value = ""
                        tempPin = null
                        _state.value = AppLockState.SETUP
                        _message.value = "Create a 4-digit PIN"
                    }
                }
                AppLockState.VERIFY -> {
                    val savedPin = repository.appLockPin.firstOrNull()
                    if (enteredPin == savedPin) {
                        _state.value = AppLockState.DASHBOARD
                        _message.value = "Unlocked"
                    } else {
                        _error.value = "Incorrect PIN"
                        _pin.value = ""
                    }
                }
                else -> {}
            }
        }
    }

    fun toggleAppLock(packageName: String) {
        val current = _lockedApps.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        viewModelScope.launch {
            repository.setLockedApps(current)
        }
    }

    fun toggleService(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAppLockEnabled(enabled)
        }
    }

    fun verifyPin(inputPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val storedPin = repository.appLockPin.firstOrNull()
            onResult(storedPin == inputPin)
        }
    }
}
