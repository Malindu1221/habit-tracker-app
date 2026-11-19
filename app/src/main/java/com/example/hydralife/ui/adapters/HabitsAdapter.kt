package com.example.hydralife.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hydralife.R
import com.example.hydralife.data.models.Habit
import com.example.hydralife.data.models.HabitProgress
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced RecyclerView adapter for displaying habits with modern UI
 */
class HabitsAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onProgressClick: (Habit, HabitProgress) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onShareClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {
    
    private var habitsWithProgress: List<Pair<Habit, HabitProgress>> = emptyList()
    
    fun updateHabits(newHabits: List<Pair<Habit, HabitProgress>>) {
        habitsWithProgress = newHabits
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_enhanced, parent, false)
        return HabitViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val (habit, progress) = habitsWithProgress[position]
        holder.bind(habit, progress)
    }
    
    override fun getItemCount(): Int = habitsWithProgress.size
    
    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val tvHabitCategory: TextView = itemView.findViewById(R.id.tv_habit_category)
        private val tvHabitTarget: TextView = itemView.findViewById(R.id.tv_habit_target)
        private val tvHabitStreak: TextView = itemView.findViewById(R.id.tv_habit_streak)
        private val tvProgressText: TextView = itemView.findViewById(R.id.tv_progress_text)
        private val tvCompletionTime: TextView = itemView.findViewById(R.id.tv_completion_time)
        private val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progress_bar_habit)
        private val btnToggleCompletion: MaterialButton = itemView.findViewById(R.id.btn_toggle_completion)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        private val btnShare: ImageButton = itemView.findViewById(R.id.btn_share)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
        private val ivHabitIcon: ImageView = itemView.findViewById(R.id.iv_habit_icon)
        
        fun bind(habit: Habit, progress: HabitProgress) {
            // Set habit name
            tvHabitName.text = habit.name
            
            // Set habit category
            tvHabitCategory.text = habit.category
            
            // Set habit target
            tvHabitTarget.text = "Target: ${habit.targetValue} ${habit.unit}"
            
            // Set habit streak (this would need to be calculated)
            tvHabitStreak.text = "0" // Placeholder - would need to calculate actual streak
            
            // Set progress text
            tvProgressText.text = "Progress: ${progress.currentValue}/${habit.targetValue} ${habit.unit}"
            
            // Set completion time if habit is completed
            if (progress.isCompleted && progress.completionTime != null) {
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                tvCompletionTime.text = "Completed at ${timeFormat.format(progress.completionTime)}"
                tvCompletionTime.visibility = View.VISIBLE
            } else {
                tvCompletionTime.visibility = View.GONE
            }
            
            // Update progress bar
            val progressPercentage = if (habit.targetValue > 0) {
                ((progress.currentValue.toFloat() / habit.targetValue.toFloat()) * 100).toInt()
            } else {
                0
            }
            
            progressBar.progress = progressPercentage
            
            // Set button text based on completion status
            btnToggleCompletion.text = if (progress.isCompleted) {
                itemView.context.getString(R.string.mark_incomplete)
            } else {
                itemView.context.getString(R.string.mark_complete)
            }
            
            // Set habit icon color based on category
            val iconColor = when (habit.category) {
                Habit.CATEGORY_HEALTH -> R.color.success
                Habit.CATEGORY_PRODUCTIVITY -> R.color.primary
                Habit.CATEGORY_WELLNESS -> R.color.warning
                Habit.CATEGORY_PERSONAL -> R.color.accent_purple
                else -> R.color.primary
            }
            
            ivHabitIcon.setBackgroundColor(ContextCompat.getColor(itemView.context, iconColor))
            
            // Set click listeners
            btnToggleCompletion.setOnClickListener {
                onProgressClick(habit, progress)
            }
            
            btnEdit.setOnClickListener {
                onEditClick(habit)
            }
            
            btnShare.setOnClickListener {
                onShareClick(habit)
            }
            
            btnDelete.setOnClickListener {
                onDeleteClick(habit)
            }
            
            // Set item click listener
            itemView.setOnClickListener {
                onHabitClick(habit)
            }
        }
    }
}