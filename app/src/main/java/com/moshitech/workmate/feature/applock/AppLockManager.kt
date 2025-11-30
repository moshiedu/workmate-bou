package com.moshitech.workmate.feature.applock

import android.content.Context
import com.moshitech.workmate.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class AppLockManager(private val context: Context) {
    
    private val prefsRepository = UserPreferencesRepository(context)
    private val unlockedApps = mutableSetOf<String>()
    private var lastUnlockTime = 0L
    
    companion object {
        private const val UNLOCK_SESSION_DURATION = 30 * 1000L // 30 seconds
        
        @Volatile
        private var instance: AppLockManager? = null
        
        fun getInstance(context: Context): AppLockManager {
            return instance ?: synchronized(this) {
                instance ?: AppLockManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    suspend fun isAppLocked(packageName: String): Boolean {
        android.util.Log.d("AppLockManager", "=== Checking if $packageName is locked ===")
        
        // Check if app lock is enabled globally
        val isEnabled = prefsRepository.isAppLockEnabled.first()
        android.util.Log.d("AppLockManager", "App Lock Enabled: $isEnabled")
        if (!isEnabled) return false
        
        // Check if this specific app is in the locked list
        val lockedApps = prefsRepository.lockedApps.first()
        android.util.Log.d("AppLockManager", "Locked apps list: $lockedApps")
        android.util.Log.d("AppLockManager", "Is $packageName in locked list? ${lockedApps.contains(packageName)}")
        if (!lockedApps.contains(packageName)) return false
        
        // Check if app was recently unlocked
        if (unlockedApps.contains(packageName)) {
            val timeSinceUnlock = System.currentTimeMillis() - lastUnlockTime
            android.util.Log.d("AppLockManager", "App was unlocked ${timeSinceUnlock}ms ago")
            if (timeSinceUnlock < UNLOCK_SESSION_DURATION) {
                android.util.Log.d("AppLockManager", "Still in unlock session, not locking")
                return false
            } else {
                // Session expired, remove from unlocked list
                android.util.Log.d("AppLockManager", "Session expired, re-locking")
                unlockedApps.remove(packageName)
            }
        }
        
        android.util.Log.d("AppLockManager", "✅ App should be LOCKED")
        return true
    }
    
    fun unlockApp(packageName: String) {
        unlockedApps.add(packageName)
        lastUnlockTime = System.currentTimeMillis()
    }
    
    fun lockApp(packageName: String) {
        unlockedApps.remove(packageName)
    }
    
    fun lockAllApps() {
        unlockedApps.clear()
    }
    
    suspend fun verifyPin(enteredPin: String): Boolean {
        val storedPin = prefsRepository.appLockPin.first()
        return storedPin == enteredPin
    }
}
