package com.moshitech.workmate.feature.speedtest.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moshitech.workmate.data.local.AppDatabase
import com.moshitech.workmate.feature.speedtest.SpeedTestManager
import com.moshitech.workmate.feature.speedtest.data.SpeedTestResult

class SpeedTestWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val manager = SpeedTestManager(applicationContext)
            val dao = AppDatabase.getDatabase(applicationContext).speedTestDao()

            // Fetch network info
            val networkInfo = manager.fetchNetworkInfo()

            // Perform lite test (ping + lite download)
            val ping = manager.measurePing()
            val downloadSpeed = manager.measureLiteDownloadSpeed()

            // Save result
            val result = SpeedTestResult(
                timestamp = System.currentTimeMillis(),
                downloadSpeed = downloadSpeed,
                uploadSpeed = 0f, // Skip upload in background
                ping = ping,
                networkType = networkInfo.type,
                ipAddress = networkInfo.ip,
                isp = networkInfo.isp
            )
            dao.insert(result)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
