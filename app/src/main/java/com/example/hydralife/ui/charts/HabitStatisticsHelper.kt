package com.example.hydralife.ui.charts

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hydralife.R
import com.example.hydralife.data.models.Habit
import com.example.hydralife.data.repository.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Helper class for managing habit statistics dashboard
 */
class HabitStatisticsHelper(
    private val prefsManager: SharedPreferencesManager
) {
    
    /**
     * Update the overall statistics display
     *
     * @param habits List of habits to calculate statistics for
     * @param totalHabitsView TextView to display total habits count
     * @param completionRateView TextView to display completion rate
     * @param currentStreakView TextView to display current streak
     */
    fun updateOverallStats(
        habits: List<Habit>,
        totalHabitsView: TextView,
        completionRateView: TextView,
        currentStreakView: TextView
    ) {
        // Update total habits count
        totalHabitsView.text = habits.size.toString()
        
        // Calculate completion rate
        if (habits.isEmpty()) {
            completionRateView.text = "0%"
        } else {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val completedHabits = habits.count { habit ->
                val progress = prefsManager.getHabitProgressForDay(habit.id, today)
                progress?.isCompleted == true
            }
            
            val completionRate = if (habits.isNotEmpty()) {
                ((completedHabits.toFloat() / habits.size.toFloat()) * 100).roundToInt()
            } else {
                0
            }
            
            completionRateView.text = "$completionRate%"
        }
        
        // Calculate current streak
        val streakCounterHelper = StreakCounterHelper(currentStreakView, prefsManager)
        streakCounterHelper.updateStreakCounter(habits)
    }
    
    /**
     * Update the category breakdown statistics
     *
     * @param habits List of habits to calculate statistics for
     * @param container LinearLayout to add category stats to
     */
    fun updateCategoryBreakdown(habits: List<Habit>, container: LinearLayout) {
        container.removeAllViews()
        
        // Group habits by category
        val habitsByCategory = habits.groupBy { it.category }
        
        // Create stats for each category
        for ((category, categoryHabits) in habitsByCategory) {
            val view = LayoutInflater.from(container.context)
                .inflate(R.layout.item_category_stat, container, false)
            
            val categoryNameView = view.findViewById<TextView>(R.id.tv_category_name)
            val categoryCountView = view.findViewById<TextView>(R.id.tv_category_count)
            val categoryProgressView = view.findViewById<TextView>(R.id.tv_category_progress)
            
            categoryNameView.text = category
            categoryCountView.text = "${categoryHabits.size} habits"
            
            // Calculate category completion rate
            if (categoryHabits.isEmpty()) {
                categoryProgressView.text = "0% completed"
            } else {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val completedHabits = categoryHabits.count { habit ->
                    val progress = prefsManager.getHabitProgressForDay(habit.id, today)
                    progress?.isCompleted == true
                }
                
                val completionRate = if (categoryHabits.isNotEmpty()) {
                    ((completedHabits.toFloat() / categoryHabits.size.toFloat()) * 100).roundToInt()
                } else {
                    0
                }
                
                categoryProgressView.text = "$completionRate% completed"
            }
            
            container.addView(view)
        }
    }
    
    /**
     * Update the weekly progress chart
     *
     * @param habits List of habits to calculate statistics for
     * @param chartContainer LinearLayout to add chart bars to
     */
    fun updateWeeklyProgressChart(habits: List<Habit>, chartContainer: LinearLayout) {
        chartContainer.removeAllViews()
        
        // Get completion percentages for the last 7 days
        val completionPercentages = getWeeklyCompletionPercentages(habits)
        
        // Create chart bars
    val barMargin = 2
    val maxHeight = 120 // Max height in dp
    val minHeight = 4   // Ensure visibility at very low/0%
        
        for (i in completionPercentages.indices) {
            val percentage = completionPercentages[i].coerceIn(0, 100)
            
            val barView = View(chartContainer.context).apply {
                // Set width and margins
                val layoutParams = LinearLayout.LayoutParams(
                    0,
                    maxOf((percentage * maxHeight / 100).dpToPx(), minHeight.dpToPx()),
                    1.0f
                ).apply {
                    setMargins(barMargin, 0, barMargin, 0)
                }
                this.layoutParams = layoutParams
                
                // Set background color based on completion percentage
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
    
    /**
     * Get completion percentages for the last 7 days
     *
     * @param habits List of habits to calculate statistics for
     * @return Array of completion percentages for each day
     */
    private fun getWeeklyCompletionPercentages(habits: List<Habit>): IntArray {
        val percentages = IntArray(7)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        if (habits.isEmpty()) {
            return percentages
        }
        
        // Calculate completion percentage for each of the last 7 days
        for (i in 0..6) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)
            
            val completedHabits = habits.count { habit ->
                val progress = prefsManager.getHabitProgressForDay(habit.id, date)
                progress?.isCompleted ?: false
            }
            
            val percentage = if (habits.isNotEmpty()) {
                (completedHabits.toFloat() / habits.size.toFloat() * 100).toInt()
            } else {
                0
            }
            
            percentages[6 - i] = percentage
        }
        
        return percentages
    }
    
    // Helper extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
}