package com.moshitech.workmate.feature.applock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.moshitech.workmate.R
import com.moshitech.workmate.data.repository.UserPreferencesRepository
import com.moshitech.workmate.feature.applock.LockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AppLockService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repository: UserPreferencesRepository
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // Check every second
    private val unlockedApps = mutableSetOf<String>()
    private val sessionDuration = 30000L // 30 seconds session

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = UserPreferencesRepository(applicationContext)
        startForeground(1, createNotification())
        handler.post(checkRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkForegroundApp() {
        serviceScope.launch {
            val isEnabled = repository.isAppLockEnabled.firstOrNull() ?: false
            if (!isEnabled) return@launch

            val lockedApps = repository.lockedApps.firstOrNull() ?: emptySet()
            if (lockedApps.isEmpty()) return@launch

            val foregroundApp = getForegroundApp()
            if (foregroundApp != null && lockedApps.contains(foregroundApp) && !unlockedApps.contains(foregroundApp)) {
                showLockScreen(foregroundApp)
            }
        }
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10, // Last 10 seconds
            time
        )

        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    private fun showLockScreen(packageName: String) {
        val intent = Intent(this, LockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        
        // Add to unlocked session
        unlockedApps.add(packageName)
        handler.postDelayed({
            unlockedApps.remove(packageName)
        }, sessionDuration)
    }

    private fun createNotification(): Notification {
        val channelId = "app_lock_service"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Lock Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("App Lock Active")
            .setContentText("Protecting your apps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
        serviceScope.cancel()
    }
}
