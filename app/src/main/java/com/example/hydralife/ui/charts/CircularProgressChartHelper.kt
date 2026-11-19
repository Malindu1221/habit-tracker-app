package com.example.hydralife.ui.charts

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hydralife.R

/**
 * Helper class for managing circular progress chart visualization
 */
class CircularProgressChartHelper(
    private val backgroundProgressBar: ProgressBar,
    private val foregroundProgressBar: ProgressBar,
    private val percentageTextView: TextView,
    private val progressTextView: TextView
) {
    
    /**
     * Update the circular progress chart with new values
     *
     * @param completedCount Number of completed habits
     * @param totalCount Total number of habits
     */
    fun updateProgress(completedCount: Int, totalCount: Int) {
        val percentage = if (totalCount > 0) {
            ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt()
        } else {
            0
        }
        
        // Update progress bars
        foregroundProgressBar.progress = percentage
        backgroundProgressBar.progress = 100  // Always full for background
        
        // Update text views
        percentageTextView.text = "$percentage%"
        progressTextView.text = "$completedCount/$totalCount completed"
        
        // Update progress bar color based on completion percentage
        val colorRes = when {
            percentage >= 80 -> R.color.success
            percentage >= 50 -> R.color.primary
            percentage >= 30 -> R.color.warning
            else -> R.color.error
        }
        
        val color = ContextCompat.getColor(foregroundProgressBar.context, colorRes)
        foregroundProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }
    
    /**
     * Show the progress chart
     */
    fun show() {
        backgroundProgressBar.visibility = View.VISIBLE
        foregroundProgressBar.visibility = View.VISIBLE
        percentageTextView.visibility = View.VISIBLE
        progressTextView.visibility = View.VISIBLE
    }
    
    /**
     * Hide the progress chart
     */
    fun hide() {
        backgroundProgressBar.visibility = View.GONE
        foregroundProgressBar.visibility = View.GONE
        percentageTextView.visibility = View.GONE
        progressTextView.visibility = View.GONE
    }
}