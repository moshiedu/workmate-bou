package com.moshitech.workmate.feature.deviceinfo.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moshitech.workmate.MainActivity
import com.moshitech.workmate.R
import com.moshitech.workmate.feature.deviceinfo.viewmodel.IntegrityCheckViewModel

class SecurityScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Perform security scan
            val viewModel = IntegrityCheckViewModel(applicationContext as android.app.Application)
            
            // Wait for scan to complete
            kotlinx.coroutines.delay(2000)
            
            // Get scan result
            val result = viewModel.scanResult.value
            
            if (result != null) {
                // Send notification with results
                sendNotification(result.score, result.overallStatus)
            }
            
            // Check for permission changes
            val permissionTracker = com.moshitech.workmate.feature.deviceinfo.utils.PermissionTracker(applicationContext)
            val changes = permissionTracker.checkForPermissionChanges()
            
            if (changes.isNotEmpty()) {
                sendPermissionChangeNotification(changes)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendNotification(score: Int, passed: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Security Scans",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Security scan results and alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val title = if (passed) "Security Scan Complete ✓" else "Security Issues Detected ⚠️"
        val text = "Security Score: $score/100"
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendPermissionChangeNotification(changes: List<String>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "permissions") // Optional: Handle navigation
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1, // Different request code
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = "Permission Changes Detected ⚠️"
        val text = if (changes.size == 1) changes.first() else "${changes.size} new permissions granted"
        
        val style = NotificationCompat.InboxStyle()
        changes.take(5).forEach { style.addLine(it) }
        if (changes.size > 5) style.addLine("+${changes.size - 5} more")
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(PERMISSION_NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "security_scans"
        const val NOTIFICATION_ID = 1001
        const val PERMISSION_NOTIFICATION_ID = 1002
        const val WORK_NAME = "security_scan_work"
    }
}
