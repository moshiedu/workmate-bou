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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppLockState {
    SETUP, // Setting up a new PIN
    CONFIRM_SETUP, // Confirming the new PIN
    SECURITY_SETUP, // Setting up security question
    VERIFY, // Unlocking the app
    FORGOT_PIN, // Forgot PIN recovery
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
    
    private val _securityQuestion = MutableStateFlow("")
    val securityQuestion: StateFlow<String> = _securityQuestion.asStateFlow()
    
    private val _securityAnswer = MutableStateFlow("")
    val securityAnswer: StateFlow<String> = _securityAnswer.asStateFlow()
    
    private val _recoveryAnswer = MutableStateFlow("")
    val recoveryAnswer: StateFlow<String> = _recoveryAnswer.asStateFlow()

    // Search and Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    enum class AppLockFilter { ALL, LOCKED, UNLOCKED }
    private val _filterType = MutableStateFlow(AppLockFilter.ALL)
    val filterType: StateFlow<AppLockFilter> = _filterType.asStateFlow()

    // App List Data
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    
    private val _lockedApps = MutableStateFlow<Set<String>>(emptySet())
    val lockedApps: StateFlow<Set<String>> = _lockedApps.asStateFlow()

    // Computed State
    val filteredApps: StateFlow<List<AppInfo>> = kotlinx.coroutines.flow.combine(
        _installedApps,
        _lockedApps,
        _searchQuery,
        _filterType
    ) { apps, locked, query, filter ->
        apps.filter { app ->
            val matchesSearch = app.name.contains(query, ignoreCase = true) || 
                              app.packageName.contains(query, ignoreCase = true)
            val isLocked = locked.contains(app.packageName)
            val matchesFilter = when (filter) {
                AppLockFilter.ALL -> true
                AppLockFilter.LOCKED -> isLocked
                AppLockFilter.UNLOCKED -> !isLocked
            }
            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<Triple<Int, Int, Int>> = kotlinx.coroutines.flow.combine(_installedApps, _lockedApps) { apps, locked ->
        Triple(
            apps.size,
            apps.count { locked.contains(it.packageName) },
            apps.count { !locked.contains(it.packageName) }
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), Triple(0, 0, 0))

    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _usePinAuth = MutableStateFlow(true)
    val usePinAuth: StateFlow<Boolean> = _usePinAuth.asStateFlow()

    private val _useBiometricAuth = MutableStateFlow(true)
    val useBiometricAuth: StateFlow<Boolean> = _useBiometricAuth.asStateFlow()

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
        viewModelScope.launch {
            repository.usePinAuth.collectLatest {
                _usePinAuth.value = it
            }
        }
        viewModelScope.launch {
            repository.useBiometricAuth.collectLatest {
                _useBiometricAuth.value = it
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: AppLockFilter) {
        _filterType.value = filter
    }

    fun togglePinAuth(enabled: Boolean) {
        viewModelScope.launch {
            // Ensure at least one method is enabled
            if (!enabled && !_useBiometricAuth.value) {
                return@launch
            }
            repository.setUsePinAuth(enabled)
        }
    }

    fun toggleBiometricAuth(enabled: Boolean) {
        viewModelScope.launch {
            // Ensure at least one method is enabled
            if (!enabled && !_usePinAuth.value) {
                return@launch
            }
            repository.setUseBiometricAuth(enabled)
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
                        _state.value = AppLockState.SECURITY_SETUP
                        _pin.value = ""
                        _message.value = "Set Security Question"
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

    fun onSecurityQuestionChange(question: String) {
        _securityQuestion.value = question
    }

    fun onSecurityAnswerChange(answer: String) {
        _securityAnswer.value = answer
    }

    fun onRecoveryAnswerChange(answer: String) {
        _recoveryAnswer.value = answer
    }

    fun saveSecurityQuestion() {
        viewModelScope.launch {
            if (_securityQuestion.value.isNotBlank() && _securityAnswer.value.isNotBlank()) {
                repository.setSecurityQuestion(_securityQuestion.value, _securityAnswer.value)
                _state.value = AppLockState.DASHBOARD
                _message.value = "Setup Complete"
            } else {
                _error.value = "Please fill in both fields"
            }
        }
    }

    fun skipSecurityQuestion() {
        _state.value = AppLockState.DASHBOARD
        _message.value = "Setup Complete"
    }

    fun showForgotPin() {
        viewModelScope.launch {
            val question = repository.securityQuestion.firstOrNull()
            if (question != null) {
                _securityQuestion.value = question
                _state.value = AppLockState.FORGOT_PIN
                _message.value = "Answer Security Question"
                _recoveryAnswer.value = ""
            } else {
                // No security question set - show reset option
                _state.value = AppLockState.FORGOT_PIN
                _message.value = "Reset PIN"
                _securityQuestion.value = "" // Empty means show reset UI
                _recoveryAnswer.value = ""
            }
        }
    }

    fun resetAppLock() {
        viewModelScope.launch {
            // Clear all app lock data
            repository.setAppLockPin("")
            repository.setLockedApps(emptySet())
            repository.setAppLockEnabled(false)
            // Go to setup
            _state.value = AppLockState.SETUP
            _message.value = "Create a 4-digit PIN"
            _pin.value = ""
            _error.value = null
        }
    }

    fun verifyRecoveryAnswer() {
        viewModelScope.launch {
            if (repository.verifySecurityAnswer(_recoveryAnswer.value)) {
                _state.value = AppLockState.SETUP
                _message.value = "Create a new 4-digit PIN"
                _pin.value = ""
                _error.value = null
            } else {
                _error.value = "Incorrect answer"
                _recoveryAnswer.value = ""
            }
        }
    }

    fun cancelRecovery() {
        _state.value = AppLockState.VERIFY
        _message.value = "Enter PIN"
        _recoveryAnswer.value = ""
        _error.value = null
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
