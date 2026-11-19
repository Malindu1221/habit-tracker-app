package com.example.hydralife.ui.charts

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hydralife.R
import com.example.hydralife.data.models.HydrationStats
import com.example.hydralife.data.repository.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HydrationStatisticsHelper(
    private val prefs: SharedPreferencesManager
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun calculateStats(): HydrationStats {
        val settings = prefs.getHydrationSettings()
        val intakes = prefs.getHydrationIntake()
        if (intakes.isEmpty()) return HydrationStats()

        val totalsByDay = intakes.groupBy { it.date }
            .mapValues { (_, list) -> list.sumOf { it.amountMl } }

        val totalDaysTracked = totalsByDay.size
        val averageDailyIntake = if (totalDaysTracked > 0) {
            (totalsByDay.values.sum().toFloat() / totalDaysTracked).roundToInt()
        } else 0

        val goalDays = totalsByDay.values.count { it >= settings.dailyGoalMl }
        val goalAchievementRate = if (totalDaysTracked > 0) {
            (goalDays.toFloat() / totalDaysTracked.toFloat() * 100f)
        } else 0f

        val (currentStreak, longestStreak) = computeStreaks(totalsByDay, settings.dailyGoalMl)

        // Weekly average here means last 7 days intake list
        val weekly = getLast7DaysIntake(totalsByDay)

        return HydrationStats(
            averageDailyIntake = averageDailyIntake,
            goalAchievementRate = goalAchievementRate,
            totalDaysTracked = totalDaysTracked,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyAverage = weekly
        )
    }

    private fun computeStreaks(totalsByDay: Map<String, Int>, goal: Int): Pair<Int, Int> {
        if (totalsByDay.isEmpty()) return 0 to 0

        val dates = totalsByDay.keys.map { dateFormat.parse(it)!! }.sorted()
        var longest = 0
        var current = 0

        var prev: Date? = null
        for (date in dates) {
            val met = totalsByDay[dateFormat.format(date)]!! >= goal
            if (!met) {
                current = 0
                prev = date
                continue
            }

            if (prev == null) {
                current = 1
            } else {
                val calPrev = Calendar.getInstance().apply { time = prev!! }
                val calCur = Calendar.getInstance().apply { time = date }
                val diffDays = daysBetween(calPrev, calCur)
                current = if (diffDays == 1) current + 1 else 1
            }
            if (current > longest) longest = current
            prev = date
        }

        // Compute current streak up to today (include missing days break)
        val today = dateFormat.format(Date())
        var curStreak = 0
        val cal = Calendar.getInstance()
        for (i in 0..365) {
            val key = dateFormat.format(cal.time)
            val total = totalsByDay[key] ?: 0
            val met = total >= goal
            if (i == 0) {
                // If today not met, streak is zero
                if (!met) break
            }
            if (met) {
                curStreak++
            } else {
                break
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return curStreak to longest
    }

    private fun daysBetween(a: Calendar, b: Calendar): Int {
        val aCopy = a.clone() as Calendar
        val bCopy = b.clone() as Calendar
        // Normalize time to midnight
        aCopy.set(Calendar.HOUR_OF_DAY, 0); aCopy.set(Calendar.MINUTE, 0); aCopy.set(Calendar.SECOND, 0); aCopy.set(Calendar.MILLISECOND, 0)
        bCopy.set(Calendar.HOUR_OF_DAY, 0); bCopy.set(Calendar.MINUTE, 0); bCopy.set(Calendar.SECOND, 0); bCopy.set(Calendar.MILLISECOND, 0)
        val diff = bCopy.timeInMillis - aCopy.timeInMillis
        return (diff / (24L * 60 * 60 * 1000)).toInt()
    }

    private fun getLast7DaysIntake(totalsByDay: Map<String, Int>): List<Int> {
        val list = IntArray(7)
        val cal = Calendar.getInstance()
        for (i in 0..6) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateFormat.format(cal.time)
            list[6 - i] = totalsByDay[key] ?: 0
        }
        return list.toList()
    }

    fun updateWeeklyProgressChart(chartContainer: LinearLayout) {
        chartContainer.removeAllViews()
        val settings = prefs.getHydrationSettings()
        val totalsByDay = prefs.getHydrationIntake().groupBy { it.date }.mapValues { (_, v) -> v.sumOf { it.amountMl } }
        val last7 = getLast7DaysIntake(totalsByDay)

        val percentages = last7.map { total ->
            if (settings.dailyGoalMl > 0) ((total.toFloat() / settings.dailyGoalMl.toFloat()) * 100).toInt().coerceIn(0, 100) else 0
        }

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

    fun bindStatsToViews(stats: HydrationStats,
                         totalDaysView: TextView,
                         avgIntakeView: TextView,
                         goalRateView: TextView,
                         currentStreakView: TextView,
                         longestStreakView: TextView) {
        totalDaysView.text = stats.totalDaysTracked.toString()
        avgIntakeView.text = "${stats.averageDailyIntake} ml"
        goalRateView.text = "${stats.goalAchievementRate.roundToInt()}%"
        currentStreakView.text = stats.currentStreak.toString()
        longestStreakView.text = stats.longestStreak.toString()
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}
