package com.example.hydralife.ui.charts

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hydralife.R
import com.example.hydralife.data.models.Habit
import com.example.hydralife.data.repository.SharedPreferencesManager
import java.util.*

/**
 * Helper class for managing habit streak counters
 */
class StreakCounterHelper(
    private val streakTextView: TextView,
    private val prefsManager: SharedPreferencesManager
) {
    
    /**
     * Update the streak counter display
     *
     * @param habits List of habits to calculate overall streak for
     */
    fun updateStreakCounter(habits: List<Habit>) {
        if (habits.isEmpty()) {
            streakTextView.text = "0 day streak"
            return
        }
        
        // Calculate the overall streak based on all habits
        val overallStreak = calculateOverallStreak(habits)
        
        // Update the text view
        streakTextView.text = when {
            overallStreak == 1 -> "$overallStreak day streak"
            overallStreak > 1 -> "$overallStreak days streak"
            else -> "0 day streak"
        }
        
        // Update text color based on streak length
        val colorRes = when {
            overallStreak >= 30 -> R.color.success
            overallStreak >= 14 -> R.color.primary
            overallStreak >= 7 -> R.color.warning
            overallStreak >= 3 -> R.color.text_primary
            else -> R.color.text_secondary
        }
        
        val color = ContextCompat.getColor(streakTextView.context, colorRes)
        streakTextView.setTextColor(color)
    }
    
    /**
     * Calculate the overall streak based on all habits
     *
     * @param habits List of habits to calculate streak for
     * @return The overall streak count
     */
    private fun calculateOverallStreak(habits: List<Habit>): Int {
        if (habits.isEmpty()) return 0
        
        // For overall streak, we'll calculate the minimum streak across all active habits
        // This ensures the streak only counts if all habits are maintained
        val streaks = habits.filter { it.isActive }.map { habit ->
            prefsManager.calculateHabitStreak(habit.id)
        }
        
        return if (streaks.isNotEmpty()) {
            streaks.minOrNull() ?: 0
        } else {
            0
        }
    }
    
    /**
     * Update streak for a specific habit
     *
     * @param habit The habit to update streak for
     * @return The current streak count for this habit
     */
    fun updateHabitStreak(habit: Habit): Int {
        val streak = prefsManager.calculateHabitStreak(habit.id)
        
        // Update the habit's stats in the repository if needed
        // (This would be implemented if we had a HabitStats repository)
        
        return streak
    }
    
    /**
     * Show the streak counter
     */
    fun show() {
        streakTextView.visibility = View.VISIBLE
    }
    
    /**
     * Hide the streak counter
     */
    fun hide() {
        streakTextView.visibility = View.GONE
    }
}