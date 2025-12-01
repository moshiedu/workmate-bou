package com.moshitech.workmate.feature.applock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppLockService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var lockManager: AppLockManager
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        lockManager = AppLockManager.getInstance(applicationContext)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                
                // Ignore our own app and system UI
                if (packageName == this.packageName || 
                    packageName.startsWith("com.android.systemui") ||
                    packageName.startsWith("com.google.android.apps.nexuslauncher") ||
                    packageName.contains("launcher")) {
                    return
                }
                
                // Don't lock the lock screen itself
                if (event.className?.toString()?.contains("LockScreenActivity") == true) {
                    return
                }
                
                // Check if this app should be locked
                serviceScope.launch {
                    android.util.Log.d("AppLockService", "Checking package: $packageName")
                    if (lockManager.isAppLocked(packageName)) {
                        android.util.Log.d("AppLockService", "Showing lock screen for: $packageName")
                        showLockScreen(packageName)
                    }
                }
            }
        }
    }
    
    private fun showLockScreen(packageName: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("locked_package", packageName)
        }
        startActivity(intent)
    }
    
    override fun onInterrupt() {
        // Required override - called when service is interrupted
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines in this scope
    }
}
