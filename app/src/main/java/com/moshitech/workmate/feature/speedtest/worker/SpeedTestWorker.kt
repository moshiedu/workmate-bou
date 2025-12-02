package com.moshitech.workmate.feature.speedtest.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.speedtest.SpeedTestManager
import com.moshitech.workmate.feature.speedtest.data.SpeedTestResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SpeedTestWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val speedTestManager = SpeedTestManager(context)
    private val dao = AppDatabase.getDatabase(context).speedTestDao()

    override suspend fun doWork(): Result {
        return try {
            // 1. Measure Ping, Jitter, Packet Loss
            val pingResult = speedTestManager.measureAdvancedPing()

            // 2. Measure Download (Default to Cloudflare if no server selected in background)
            val downloadSpeed = speedTestManager.measureDownloadSpeed("https://speed.cloudflare.com/__down?bytes=25000000") { _, _ -> }

            // 3. Measure Upload
            val uploadSpeed = speedTestManager.measureUploadSpeed("https://speed.cloudflare.com/__up", { _, _ -> }, { _ -> })

            // 4. Save Result
            val networkInfo = speedTestManager.fetchNetworkInfo()

            val result = SpeedTestResult(
                timestamp = System.currentTimeMillis(),
                downloadSpeed = downloadSpeed,
                uploadSpeed = uploadSpeed,
                ping = pingResult.ping,
                networkType = networkInfo.type,
                ipAddress = networkInfo.ip,
                isp = networkInfo.isp,
                jitter = pingResult.jitter,
                packetLoss = pingResult.packetLoss
            )
            dao.insert(result)

            // 5. Send Notification
            sendNotification(result)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun sendNotification(result: SpeedTestResult) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "speed_test_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Speed Test Results",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val date = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(result.timestamp))
        val contentText = "Down: %.1f Mbps | Up: %.1f Mbps | Ping: %d ms".format(result.downloadSpeed, result.uploadSpeed, result.ping)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_sort_by_size) // Use a generic system icon
            .setContentTitle("Speed Test Completed ($date)")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
