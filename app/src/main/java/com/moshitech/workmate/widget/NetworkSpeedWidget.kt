package com.moshitech.workmate.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.moshitech.workmate.MainActivity
import com.moshitech.workmate.R

class NetworkSpeedWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, 0L, 0L)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start the service when first widget is added
        val serviceIntent = Intent(context, NetworkSpeedService::class.java)
        context.startForegroundService(serviceIntent)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // We don't stop the service here anymore. 
        // The user might want the floating widget to persist even if the home screen widget is removed.
        // The service can be stopped via the App UI.
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == NetworkSpeedService.ACTION_UPDATE_WIDGET) {
            val downloadSpeed = intent.getLongExtra(NetworkSpeedService.EXTRA_DOWNLOAD_SPEED, 0L)
            val uploadSpeed = intent.getLongExtra(NetworkSpeedService.EXTRA_UPLOAD_SPEED, 0L)
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = android.content.ComponentName(context, NetworkSpeedWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, downloadSpeed, uploadSpeed)
            }
        }
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            downloadSpeed: Long,
            uploadSpeed: Long
        ) {
            val views = RemoteViews(context.packageName, R.layout.network_speed_widget)
            
            // Update speeds
            views.setTextViewText(R.id.download_speed, "↓ ${formatSpeed(downloadSpeed)}")
            views.setTextViewText(R.id.upload_speed, "↑ ${formatSpeed(uploadSpeed)}")
            
            // Set click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun formatSpeed(bytesPerSecond: Long): String {
            return when {
                bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
                bytesPerSecond < 1024 * 1024 -> String.format("%.1f KB/s", bytesPerSecond / 1024.0)
                else -> String.format("%.2f MB/s", bytesPerSecond / (1024.0 * 1024.0))
            }
        }
    }
}
