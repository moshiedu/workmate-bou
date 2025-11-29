package com.moshitech.workmate.widget

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.TrafficStats
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.moshitech.workmate.MainActivity
import com.moshitech.workmate.R
import kotlinx.coroutines.*

class NetworkSpeedService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTime = 0L
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var downloadTextView: TextView? = null
    private var uploadTextView: TextView? = null
    
    companion object {
        const val ACTION_UPDATE_WIDGET = "com.moshitech.workmate.ACTION_UPDATE_WIDGET"
        const val EXTRA_DOWNLOAD_SPEED = "download_speed"
        const val EXTRA_UPLOAD_SPEED = "upload_speed"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "network_speed_monitor"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(0L, 0L))
        createFloatingView()
        startSpeedMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        removeFloatingView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingView() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating_speed_view, null)
            downloadTextView = floatingView?.findViewById(R.id.download_speed_text)
            uploadTextView = floatingView?.findViewById(R.id.upload_speed_text)
            
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            
            layoutParams.gravity = Gravity.TOP or Gravity.END
            layoutParams.x = 10
            layoutParams.y = 100
            
            // Make it draggable
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            
            floatingView?.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (initialTouchX - event.rawX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, layoutParams)
                        true
                    }
                    else -> false
                }
            }
            
            windowManager?.addView(floatingView, layoutParams)
        } catch (e: Exception) {
            // Permission not granted or error creating overlay
            e.printStackTrace()
        }
    }
    
    private fun removeFloatingView() {
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }

    private fun startSpeedMonitoring() {
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()

        serviceScope.launch {
            while (isActive) {
                delay(1000) // Update every second
                
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()
                val currentTime = System.currentTimeMillis()
                
                val timeDelta = (currentTime - lastTime) / 1000.0 // seconds
                
                if (timeDelta > 0) {
                    val downloadSpeed = ((currentRxBytes - lastRxBytes) / timeDelta).toLong()
                    val uploadSpeed = ((currentTxBytes - lastTxBytes) / timeDelta).toLong()
                    
                    // Update floating view
                    withContext(Dispatchers.Main) {
                        downloadTextView?.text = "↓ ${formatSpeed(downloadSpeed)}"
                        uploadTextView?.text = "↑ ${formatSpeed(uploadSpeed)}"
                    }
                    
                    // Update notification
                    val notification = createNotification(downloadSpeed, uploadSpeed)
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                    
                    // Broadcast to widget
                    updateWidget(downloadSpeed, uploadSpeed)
                    
                    lastRxBytes = currentRxBytes
                    lastTxBytes = currentTxBytes
                    lastTime = currentTime
                }
            }
        }
    }

    private fun updateWidget(downloadSpeed: Long, uploadSpeed: Long) {
        val intent = Intent(ACTION_UPDATE_WIDGET).apply {
            putExtra(EXTRA_DOWNLOAD_SPEED, downloadSpeed)
            putExtra(EXTRA_UPLOAD_SPEED, uploadSpeed)
        }
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Speed Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time network speed"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(downloadSpeed: Long, uploadSpeed: Long): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val downloadText = formatSpeed(downloadSpeed)
        val uploadText = formatSpeed(uploadSpeed)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("↓$downloadText ↑$uploadText")
            .setContentText("Network Speed Monitor")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .build()
    }

    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "${bytesPerSecond}B/s"
            bytesPerSecond < 1024 * 1024 -> String.format("%.1fK/s", bytesPerSecond / 1024.0)
            else -> String.format("%.1fM/s", bytesPerSecond / (1024.0 * 1024.0))
        }
    }
}
