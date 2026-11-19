package com.example.hydralife.ui.charts

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.example.hydralife.R
import com.example.hydralife.data.models.Habit
import com.example.hydralife.data.repository.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for managing quick habit actions and shortcuts
 */
class QuickHabitActionsHelper(
    private val prefsManager: SharedPreferencesManager,
    private val onHabitAdded: () -> Unit,
    private val onStatsViewed: () -> Unit,
    private val onMotivationViewed: () -> Unit
) {
    
    /**
     * Setup quick action buttons
     *
     * @param quickAddCard The card view for adding habits
     * @param viewStatsCard The card view for viewing statistics
     * @param motivationCard The card view for viewing motivation
     */
    fun setupQuickActionButtons(
        quickAddCard: CardView,
        viewStatsCard: CardView,
        motivationCard: CardView
    ) {
        // Setup quick add habit button
        quickAddCard.setOnClickListener {
            onHabitAdded()
        }
        
        // Setup view statistics button
        viewStatsCard.setOnClickListener {
            onStatsViewed()
        }
        
        // Setup motivation button
        motivationCard.setOnClickListener {
            onMotivationViewed()
        }
    }
    
    /**
     * Get the most frequently completed habits
     *
     * @param habits List of habits to analyze
     * @param count Number of habits to return
     * @return List of most frequently completed habits
     */
    fun getMostFrequentHabits(habits: List<Habit>, count: Int): List<Habit> {
        val habitCompletionMap = mutableMapOf<String, Int>()
        
        // Count completions for each habit
        for (habit in habits) {
            val progressList = prefsManager.getHabitProgress().filter { 
                it.habitId == habit.id && it.isCompleted 
            }
            habitCompletionMap[habit.id] = progressList.size
        }
        
        // Sort habits by completion count and return top habits
        return habits.sortedByDescending { habitCompletionMap[it.id] ?: 0 }.take(count)
    }
    
    /**
     * Get habits that are due today (not yet completed)
     *
     * @param habits List of habits to check
     * @return List of habits due today
     */
    fun getHabitsDueToday(habits: List<Habit>): List<Habit> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        return habits.filter { habit ->
            val progress = prefsManager.getHabitProgressForDay(habit.id, today)
            progress?.isCompleted != true
        }
    }
    
    /**
     * Create a quick completion button for a habit
     *
     * @param habit The habit to create a button for
     * @param container The container to add the button to
     */
    fun createQuickCompletionButton(habit: Habit, container: LinearLayout) {
        val button = Button(container.context).apply {
            text = "✓ ${habit.name}"
            setTextColor(container.context.getColor(R.color.on_primary))
            setBackgroundColor(container.context.getColor(R.color.primary))
            setPadding(16, 8, 16, 8)
            textSize = 12f
            
            // Set click listener to mark habit as complete
            setOnClickListener {
                markHabitComplete(habit)
            }
        }
        
        container.addView(button)
    }
    
    /**
     * Mark a habit as complete for today
     *
     * @param habit The habit to mark as complete
     */
    private fun markHabitComplete(habit: Habit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existingProgress = prefsManager.getHabitProgressForDay(habit.id, today)
        
        val newProgress = if (existingProgress != null) {
            existingProgress.copy(
                isCompleted = true,
                currentValue = habit.targetValue,
                completionTime = Date()
            )
        } else {
            com.example.hydralife.data.models.HabitProgress(
                habitId = habit.id,
                date = today,
                currentValue = habit.targetValue,
                isCompleted = true,
                completionTime = Date()
            )
        }
        
        prefsManager.saveHabitProgressForDay(habit.id, today, newProgress)
    }
}