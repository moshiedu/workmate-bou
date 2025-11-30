package com.moshitech.workmate.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.moshitech.workmate.MainActivity
import com.moshitech.workmate.R
import java.io.RandomAccessFile
import kotlin.math.abs

class FloatingMonitorService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 3
        private const val CHANNEL_ID = "floating_monitor_channel"
        var isServiceRunning = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private val handler = Handler(Looper.getMainLooper())
    private var lastRxBytes: Long = 0
    private var lastTxBytes: Long = 0
    private var lastTime: Long = 0

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateStats()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        startForeground(NOTIFICATION_ID, createNotification())

        if (Settings.canDrawOverlays(this)) {
            setupFloatingView()
            lastRxBytes = TrafficStats.getTotalRxBytes()
            lastTxBytes = TrafficStats.getTotalTxBytes()
            lastTime = System.currentTimeMillis()
            handler.post(updateRunnable)
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        handler.removeCallbacks(updateRunnable)
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
    }

    private fun setupFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_monitor_layout, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params?.gravity = Gravity.TOP or Gravity.START
        params?.x = 0
        params?.y = 100

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager?.addView(floatingView, params)

        setupTouchListener()
    }

    private fun setupTouchListener() {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params?.x ?: 0
                        initialY = params?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> return true
                    MotionEvent.ACTION_MOVE -> {
                        params?.x = initialX + (event.rawX - initialTouchX).toInt()
                        params?.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun updateStats() {
        if (floatingView == null) return

        // Network Speed
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()
        val timeDelta = (currentTime - lastTime) / 1000.0

        if (timeDelta > 0) {
            val rxSpeed = ((currentRxBytes - lastRxBytes) / timeDelta).toLong()
            val txSpeed = ((currentTxBytes - lastTxBytes) / timeDelta).toLong()
            
            val speedText = "${formatSpeed(rxSpeed)} ↓  ${formatSpeed(txSpeed)} ↑"
            floatingView?.findViewById<TextView>(R.id.network_text)?.text = speedText
        }

        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
        lastTime = currentTime

        // CPU Usage
        val cpuUsage = getCpuUsage()
        floatingView?.findViewById<TextView>(R.id.cpu_text)?.text = "CPU: ${cpuUsage}%"

        // RAM Usage
        val ramUsage = getRamUsage()
        floatingView?.findViewById<TextView>(R.id.ram_text)?.text = "RAM: ${ramUsage}%"
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024.0)
            else -> "$bytesPerSec B/s"
        }
    }

    private fun getCpuUsage(): Int {
        try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()

            val toks = load.split(" +".toRegex()).toTypedArray()
            val idle1 = toks[4].toLong()
            val cpu1 = toks[2].toLong() + toks[3].toLong() + toks[5].toLong() + toks[6].toLong() + toks[7].toLong() + toks[8].toLong()

            Thread.sleep(360)

            val reader2 = RandomAccessFile("/proc/stat", "r")
            val load2 = reader2.readLine()
            reader2.close()

            val toks2 = load2.split(" +".toRegex()).toTypedArray()
            val idle2 = toks2[4].toLong()
            val cpu2 = toks2[2].toLong() + toks2[3].toLong() + toks2[5].toLong() + toks2[6].toLong() + toks2[7].toLong() + toks2[8].toLong()

            return if (cpu2 - cpu1 + idle2 - idle1 == 0L) 0 else ((cpu2 - cpu1) * 100 / (cpu2 - cpu1 + idle2 - idle1)).toInt()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return 0
    }

    private fun getRamUsage(): Int {
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalMem = memoryInfo.totalMem
        val availMem = memoryInfo.availMem
        val usedMem = totalMem - availMem
        
        return ((usedMem.toDouble() / totalMem.toDouble()) * 100).toInt()
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Floating Monitor")
            .setContentText("Monitoring system resources...")
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
                "Floating Monitor Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Shows floating hardware monitor"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
