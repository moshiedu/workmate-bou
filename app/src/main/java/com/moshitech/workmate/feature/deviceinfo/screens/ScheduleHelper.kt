package com.moshitech.workmate.feature.deviceinfo.screens

import android.content.Context

fun getCurrentScheduleFrequency(context: Context): String {
    return try {
        val workManager = androidx.work.WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(
            com.moshitech.workmate.feature.deviceinfo.workers.SecurityScanWorker.WORK_NAME
        ).get()
        
        if (workInfos.isNotEmpty() && workInfos[0].state == androidx.work.WorkInfo.State.ENQUEUED) {
            val nextRunTime = workInfos[0].nextScheduleTimeMillis
            if (nextRunTime > 0) {
                val hoursUntil = (nextRunTime - System.currentTimeMillis()) / (1000 * 60 * 60)
                when {
                    hoursUntil < 30 -> "DAILY"
                    hoursUntil < 200 -> "WEEKLY"
                    else -> "MONTHLY"
                }
            } else {
                "NONE"
            }
        } else {
            "NONE"
        }
    } catch (e: Exception) {
        "NONE"
    }
}
