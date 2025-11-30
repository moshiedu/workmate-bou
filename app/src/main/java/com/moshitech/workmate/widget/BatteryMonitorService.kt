package com.moshitech.workmate.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.moshitech.workmate.MainActivity
import com.moshitech.workmate.R
import kotlinx.coroutines.*

class BatteryMonitorService : Service() {

    companion object {
        const val ACTION_BATTERY_UPDATE = "com.moshitech.workmate.BATTERY_UPDATE"
        const val EXTRA_BATTERY_LEVEL = "battery_level"
        const val EXTRA_IS_CHARGING = "is_charging"
        const val EXTRA_TEMPERATURE = "temperature"
        const val EXTRA_VOLTAGE = "voltage"
        const val EXTRA_HEALTH = "health"
        const val EXTRA_TECHNOLOGY = "technology"
        const val EXTRA_STATUS = "status"
        
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "battery_monitor_channel"
        
        var isServiceRunning = false
            private set
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var batteryReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        startForeground(NOTIFICATION_ID, createNotification(0, false, 0f, 0))
        registerBatteryReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        unregisterBatteryReceiver()
        serviceScope.cancel()
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { processBatteryIntent(it) }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
        
        // Get initial battery status
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryStatus?.let { processBatteryIntent(it) }
    }

    private fun unregisterBatteryReceiver() {
        batteryReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun processBatteryIntent(intent: Intent) {
        // Extract battery metrics
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) {
            (level / scale.toFloat() * 100).toInt()
        } else {
            0
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val tempCelsius = temperature / 10.0f

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val healthText = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        val statusText = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }

        // Update notification
        val notification = createNotification(batteryPct, isCharging, tempCelsius, voltage)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Broadcast to widget
        broadcastBatteryUpdate(batteryPct, isCharging, tempCelsius, voltage, healthText, technology, statusText)
    }

    private fun broadcastBatteryUpdate(
        level: Int,
        isCharging: Boolean,
        temperature: Float,
        voltage: Int,
        health: String,
        technology: String,
        status: String
    ) {
        val intent = Intent(ACTION_BATTERY_UPDATE).apply {
            putExtra(EXTRA_BATTERY_LEVEL, level)
            putExtra(EXTRA_IS_CHARGING, isCharging)
            putExtra(EXTRA_TEMPERATURE, temperature)
            putExtra(EXTRA_VOLTAGE, voltage)
            putExtra(EXTRA_HEALTH, health)
            putExtra(EXTRA_TECHNOLOGY, technology)
            putExtra(EXTRA_STATUS, status)
        }
        sendBroadcast(intent)
    }

    private fun createNotification(
        batteryLevel: Int,
        isCharging: Boolean,
        temperature: Float,
        voltage: Int
    ): Notification {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusIcon = if (isCharging) "⚡" else "🔋"
        val contentText = "$statusIcon $batteryLevel% • ${String.format("%.1f", temperature)}°C • ${voltage}mV"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Battery Monitor")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Shows battery monitoring status"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
