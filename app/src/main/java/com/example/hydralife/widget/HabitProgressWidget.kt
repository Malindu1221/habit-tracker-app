package com.example.hydralife.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.hydralife.MainActivity
import com.example.hydralife.R
import com.example.hydralife.data.repository.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home screen widget showing today's habit completion progress
 */
class HabitProgressWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefsManager = SharedPreferencesManager.getInstance(context)
            val habits = prefsManager.getHabits()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Calculate completion statistics
            val completedCount = if (habits.isNotEmpty()) {
                habits.count { habit ->
                    val progress = prefsManager.getHabitProgressForDay(habit.id, today)
                    progress?.isCompleted == true
                }
            } else {
                0
            }
            
            val totalCount = habits.size
            val progressPercentage = if (totalCount > 0) {
                ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt()
            } else {
                0
            }

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_habit_progress)
            
            // Update widget content
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
            views.setTextViewText(
                R.id.widget_progress_text,
                context.getString(R.string.widget_habits_completed, completedCount, totalCount)
            )
            views.setTextViewText(
                R.id.widget_progress_percentage,
                "$progressPercentage%"
            )
            
            // Update chart bars with simulated data (in a real app, this would come from actual habit history)
            updateChartBars(views, context)
            
            // Set up click intent to open the app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        private fun updateChartBars(views: RemoteViews, context: Context) {
            // In a real implementation, this would show actual habit completion data over the last 7 days
            // For now, we'll use simulated data
            val barIds = arrayOf(
                R.id.bar_0, R.id.bar_1, R.id.bar_2, R.id.bar_3, 
                R.id.bar_4, R.id.bar_5, R.id.bar_6
            )
            
            // Simulated heights as percentages (0-100)
            val heights = arrayOf(20, 30, 50, 80, 70, 40, 20)
            
            for (i in barIds.indices) {
                // Set the height as a percentage of the max height (60dp)
                // We'll simulate this by changing the color based on height
                val heightPercent = heights[i]
                val colorRes = if (heightPercent > 60) R.color.primary else R.color.progress_incomplete
                views.setInt(barIds[i], "setBackgroundResource", colorRes)
            }
        }
        
        // Remove the old updateProgressSegments function since we're not using segments anymore
    }
}