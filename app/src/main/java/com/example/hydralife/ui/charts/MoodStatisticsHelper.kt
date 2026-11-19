package com.example.hydralife.ui.charts

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hydralife.R
import com.example.hydralife.data.models.MoodEntry
import com.example.hydralife.data.models.MoodStats
import com.example.hydralife.data.models.MoodType
import com.example.hydralife.data.repository.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MoodStatisticsHelper(private val prefs: SharedPreferencesManager) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun calculateStats(): MoodStats {
        val entries = prefs.getMoodEntries()
        if (entries.isEmpty()) return MoodStats()

        val totalEntries = entries.size
        val moodCounts = entries.groupingBy { it.mood }.eachCount()
        val most = moodCounts.maxByOrNull { it.value }?.key ?: MoodType.NEUTRAL
        val avgMood = entries.map { it.mood.value }.average().toFloat()

        val weeklyTrend = getLast7DaysAverages(entries)

        return MoodStats(
            averageMood = avgMood,
            mostFrequentMood = most,
            totalEntries = totalEntries,
            moodCounts = moodCounts,
            weeklyTrend = weeklyTrend
        )
    }

    private fun getLast7DaysAverages(entries: List<MoodEntry>): List<Float> {
        val result = FloatArray(7)
        val cal = Calendar.getInstance()
        for (i in 0..6) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateFormat.format(cal.time)
            val dayEntries = entries.filter { it.date == key }
            result[6 - i] = if (dayEntries.isNotEmpty()) dayEntries.map { it.mood.value }.average().toFloat() else 0f
        }
        return result.toList()
    }

    fun updateWeeklyMoodChart(chartContainer: LinearLayout) {
        chartContainer.removeAllViews()

        val weekly = getLast7DaysAverages(prefs.getMoodEntries())
        // Convert 1..5 scale to percentage-like height for visibility
        val percentages = weekly.map { ((it / 5f) * 100f).toInt().coerceIn(0, 100) }

        val barMargin = 2
        val maxHeight = 120 // dp
        val minHeight = 4   // dp

        for (percentage in percentages) {
            val barView = View(chartContainer.context).apply {
                val lp = LinearLayout.LayoutParams(
                    0,
                    maxOf((percentage * maxHeight / 100).dpToPx(), minHeight.dpToPx()),
                    1f
                ).apply { setMargins(barMargin, 0, barMargin, 0) }
                layoutParams = lp
                val colorRes = when {
                    percentage >= 80 -> R.color.success
                    percentage >= 50 -> R.color.primary
                    percentage >= 30 -> R.color.warning
                    else -> R.color.error
                }
                setBackgroundColor(ContextCompat.getColor(context, colorRes))
            }
            chartContainer.addView(barView)
        }
    }

    fun bindStatsToViews(stats: MoodStats,
                         avgEmojiView: TextView,
                         moodDescView: TextView,
                         totalEntriesView: TextView,
                         mostFrequentView: TextView,
                         consistencyView: TextView) {
        totalEntriesView.text = stats.totalEntries.toString()
        mostFrequentView.text = stats.mostFrequentMood.emoji
        // Show the rounded average mood as an emoji using closest mood
        val rounded = stats.averageMood.roundToInt().coerceIn(1, 5)
        val closest = MoodType.values().minByOrNull { kotlin.math.abs(it.value - rounded) } ?: MoodType.NEUTRAL
        avgEmojiView.text = closest.emoji
        moodDescView.text = closest.label

        // Consistency: how many of last 7 days had at least one mood entry
        val last7Consistency = stats.weeklyTrend.count { it > 0f }
        val consistencyPct = (last7Consistency / 7f * 100f).roundToInt()
        consistencyView.text = "$consistencyPct%"
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}
