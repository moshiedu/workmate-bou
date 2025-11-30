package com.moshitech.workmate.feature.deviceinfo.utils

import android.content.Context
import androidx.work.*
import com.moshitech.workmate.feature.deviceinfo.workers.SecurityScanWorker
import java.util.concurrent.TimeUnit

object ScanScheduler {

    enum class ScanFrequency(val hours: Long) {
        DAILY(24),
        WEEKLY(168),  // 7 days
        MONTHLY(720)  // 30 days
    }

    fun scheduleScan(context: Context, frequency: ScanFrequency) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(false)
            .build()

        val scanRequest = PeriodicWorkRequestBuilder<SecurityScanWorker>(
            frequency.hours,
            TimeUnit.HOURS,
            15, // Flex interval
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("security_scan")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SecurityScanWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            scanRequest
        )
    }

    fun cancelScheduledScans(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SecurityScanWorker.WORK_NAME)
    }

    fun runImmediateScan(context: Context) {
        val scanRequest = OneTimeWorkRequestBuilder<SecurityScanWorker>()
            .addTag("security_scan_immediate")
            .build()

        WorkManager.getInstance(context).enqueue(scanRequest)
    }
}
