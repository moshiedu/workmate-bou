package com.moshitech.workmate.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.widget.RemoteViews
import com.moshitech.workmate.MainActivity
import com.moshitech.workmate.R

class BatteryMonitorWidget : AppWidgetProvider() {

    private var batteryReceiver: BroadcastReceiver? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, 0, false, 0f, 0, "Unknown", "Unknown", "Unknown")
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        
        // Start battery monitor service
        val serviceIntent = Intent(context, BatteryMonitorService::class.java)
        context.startForegroundService(serviceIntent)
        
        // Register receiver for battery updates
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BatteryMonitorService.ACTION_BATTERY_UPDATE) {
                    val level = intent.getIntExtra(BatteryMonitorService.EXTRA_BATTERY_LEVEL, 0)
                    val isCharging = intent.getBooleanExtra(BatteryMonitorService.EXTRA_IS_CHARGING, false)
                    val temperature = intent.getFloatExtra(BatteryMonitorService.EXTRA_TEMPERATURE, 0f)
                    val voltage = intent.getIntExtra(BatteryMonitorService.EXTRA_VOLTAGE, 0)
                    val health = intent.getStringExtra(BatteryMonitorService.EXTRA_HEALTH) ?: "Unknown"
                    val technology = intent.getStringExtra(BatteryMonitorService.EXTRA_TECHNOLOGY) ?: "Unknown"
                    val status = intent.getStringExtra(BatteryMonitorService.EXTRA_STATUS) ?: "Unknown"
                    
                    context?.let { ctx ->
                        val appWidgetManager = AppWidgetManager.getInstance(ctx)
                        val appWidgetIds = appWidgetManager.getAppWidgetIds(
                            android.content.ComponentName(ctx, BatteryMonitorWidget::class.java)
                        )
                        
                        for (appWidgetId in appWidgetIds) {
                            updateAppWidget(ctx, appWidgetManager, appWidgetId, level, isCharging, 
                                temperature, voltage, health, technology, status)
                        }
                    }
                }
            }
        }
        
        val filter = IntentFilter(BatteryMonitorService.ACTION_BATTERY_UPDATE)
        context.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        
        // Unregister receiver
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Stop service when last widget is removed
        val serviceIntent = Intent(context, BatteryMonitorService::class.java)
        context.stopService(serviceIntent)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        batteryLevel: Int,
        isCharging: Boolean,
        temperature: Float,
        voltage: Int,
        health: String,
        technology: String,
        status: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.battery_monitor_widget)

        // Update battery percentage
        views.setTextViewText(R.id.battery_percentage, "$batteryLevel%")
        
        // Update charging status
        val statusIcon = if (isCharging) "⚡" else "🔋"
        views.setTextViewText(R.id.battery_status, "$statusIcon $status")
        
        // Update temperature
        views.setTextViewText(R.id.battery_temperature, "${String.format("%.1f", temperature)}°C")
        
        // Update voltage
        views.setTextViewText(R.id.battery_voltage, "${voltage}mV")
        
        // Update health
        views.setTextViewText(R.id.battery_health, "Health: $health")
        
        // Color code based on battery level
        val color = when {
            batteryLevel >= 60 -> Color.parseColor("#4CAF50") // Green
            batteryLevel >= 20 -> Color.parseColor("#FF9800") // Orange
            else -> Color.parseColor("#F44336") // Red
        }
        views.setTextColor(R.id.battery_percentage, color)
        
        // Set click intent to open battery settings
        val intent = Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
