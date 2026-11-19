package com.example.hydralife.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hydralife.R
import com.example.hydralife.data.models.MoodEntry
import com.example.hydralife.data.models.MoodType
import com.example.hydralife.data.repository.SharedPreferencesManager
import com.example.hydralife.ui.adapters.MoodSelectorAdapter
import com.example.hydralife.ui.adapters.MoodHistoryAdapter
import com.example.hydralife.ui.charts.MoodChartHelper
import com.example.hydralife.ui.charts.MoodStatisticsHelper
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for mood journaling with emoji selector
 */
class MoodFragment : Fragment() {
    
    private lateinit var recyclerMoodSelector: RecyclerView
    private lateinit var recyclerMoodHistory: RecyclerView
    private lateinit var btnSaveMood: MaterialButton
    private lateinit var btnShareMood: MaterialButton
    private lateinit var tabLayoutMoodView: TabLayout
    private lateinit var layoutMoodCalendar: View
    
    // New views for enhanced UI
    private lateinit var tvAverageMood: TextView
    private lateinit var tvMoodDescription: TextView
    private lateinit var tvTotalEntries: TextView
    private lateinit var tvMostFrequent: TextView
    private lateinit var tvConsistency: TextView
    private lateinit var circularProgressBarBackground: ProgressBar
    private lateinit var circularProgressBarForeground: ProgressBar
    private lateinit var cardQuickMood: MaterialCardView
    private lateinit var cardViewStats: MaterialCardView
    private lateinit var cardShareMood: MaterialCardView
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var moodSelectorAdapter: MoodSelectorAdapter
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    private lateinit var moodChartHelper: MoodChartHelper
    private lateinit var moodStatsHelper: MoodStatisticsHelper
    
    private var selectedMood: MoodType? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupMoodSelector()
        setupMoodHistory()
        setupMoodViewTabs()
        setupClickListeners()
        loadMoodHistory()
    }
    
    override fun onResume() {
        super.onResume()
        loadMoodHistory()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        moodChartHelper = MoodChartHelper(requireContext())
        moodStatsHelper = MoodStatisticsHelper(prefsManager)
        recyclerMoodSelector = view.findViewById(R.id.recycler_mood_selector)
        recyclerMoodHistory = view.findViewById(R.id.recycler_mood_history)
        btnSaveMood = view.findViewById(R.id.btn_save_mood)
        btnShareMood = view.findViewById(R.id.btn_share_mood)
        tabLayoutMoodView = view.findViewById(R.id.tab_layout_mood_view)
        layoutMoodCalendar = view.findViewById(R.id.layout_mood_calendar)
        
        // New views for enhanced UI
        tvAverageMood = view.findViewById(R.id.tv_average_mood)
        tvMoodDescription = view.findViewById(R.id.tv_mood_description)
        tvTotalEntries = view.findViewById(R.id.tv_total_entries)
        tvMostFrequent = view.findViewById(R.id.tv_most_frequent)
        tvConsistency = view.findViewById(R.id.tv_consistency)
        circularProgressBarBackground = view.findViewById(R.id.circular_progress_bar_background)
        circularProgressBarForeground = view.findViewById(R.id.circular_progress_bar_foreground)
        cardQuickMood = view.findViewById(R.id.card_quick_mood)
        cardViewStats = view.findViewById(R.id.card_view_stats)
        cardShareMood = view.findViewById(R.id.card_share_mood)
    }
    
    private fun setupMoodSelector() {
        moodSelectorAdapter = MoodSelectorAdapter { mood ->
            selectedMood = mood
            updateSaveButtonState()
        }
        
        recyclerMoodSelector.apply {
            layoutManager = GridLayoutManager(context, 5) // 5 columns for emojis
            adapter = moodSelectorAdapter
            setHasFixedSize(true)
        }
        
        // Load all available moods
        moodSelectorAdapter.updateMoods(MoodType.getAllMoods())
    }
    
    private fun setupMoodHistory() {
        moodHistoryAdapter = MoodHistoryAdapter(
            onDeleteClick = { entry -> deleteMoodEntry(entry) },
            onShareClick = { entry -> shareMoodEntry(entry) }
        )
        
        recyclerMoodHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodHistoryAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupMoodViewTabs() {
        // Add tabs for List and Calendar views
        val listTab = tabLayoutMoodView.newTab().setText("List")
        val calendarTab = tabLayoutMoodView.newTab().setText("Calendar")
        tabLayoutMoodView.addTab(listTab)
        tabLayoutMoodView.addTab(calendarTab)
        
        // Set up tab listener
        tabLayoutMoodView.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showListView()
                    1 -> showCalendarView()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupClickListeners() {
        btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }
        
        btnShareMood.setOnClickListener {
            shareTodaysMood()
        }
        
        // Card click listeners for enhanced UI
        cardQuickMood.setOnClickListener {
            // Scroll to mood selector section
            val scrollView = view?.findViewById<androidx.core.widget.NestedScrollView>(R.id.nested_scroll_view_mood)
            if (scrollView != null) {
                scrollView.smoothScrollTo(0, recyclerMoodSelector.top)
            }
        }
        
        cardViewStats.setOnClickListener {
            showMoodStatisticsDialog()
        }
        
        cardShareMood.setOnClickListener {
            shareTodaysMood()
        }
    }
    
    private fun updateSaveButtonState() {
        btnSaveMood.isEnabled = selectedMood != null
    }
    
    private fun saveMoodEntry() {
        val mood = selectedMood ?: return
        
        val moodEntry = MoodEntry(
            mood = mood,
            emoji = mood.emoji,
            notes = "", // Removed notes field
            timestamp = Date()
        )
        
        prefsManager.saveMoodEntry(moodEntry)
        
        // Reset form
        selectedMood = null
        moodSelectorAdapter.clearSelection()
        updateSaveButtonState()
        
        // Refresh history
        loadMoodHistory()
        
        // Show success message
        android.widget.Toast.makeText(
            requireContext(),
            "Mood saved successfully!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun loadMoodHistory() {
        val moodEntries = prefsManager.getMoodEntries()
        moodHistoryAdapter.updateMoodEntries(moodEntries)
        
        // Update share button state
        val todayEntries = prefsManager.getTodayMoodEntries()
        btnShareMood.isEnabled = todayEntries.isNotEmpty()
        
        // Update empty state visibility
        val isEmpty = moodEntries.isEmpty()
        recyclerMoodHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
        view?.findViewById<View>(R.id.layout_empty_mood_history)?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        
        // Update new stats views for enhanced UI
        updateMoodStats(moodEntries)
        
        // If calendar view is visible, update it too
        if (layoutMoodCalendar.visibility == View.VISIBLE) {
            generateCalendarView()
        }
    }
    
    private fun updateMoodStats(moodEntries: List<MoodEntry>) {
        // Update total entries
        tvTotalEntries.text = moodEntries.size.toString()
        
        if (moodEntries.isEmpty()) {
            tvAverageMood.text = "😊"
            tvMoodDescription.text = "Happy"
            tvMostFrequent.text = "😊"
            tvConsistency.text = "0%"
            circularProgressBarForeground.progress = 0
            return
        }
        
        // Calculate average mood (simplified)
        val moodCounts = mutableMapOf<String, Int>()
        for (entry in moodEntries) {
            moodCounts[entry.emoji] = moodCounts.getOrDefault(entry.emoji, 0) + 1
        }
        
        // Find most frequent mood
        val mostFrequentMood = moodCounts.maxByOrNull { it.value }?.key ?: "😊"
        tvMostFrequent.text = mostFrequentMood
        
        // Set average mood to most frequent for simplicity
        tvAverageMood.text = mostFrequentMood
        
        // Set mood description
        val moodType = getMoodTypeFromEmoji(mostFrequentMood)
        tvMoodDescription.text = moodType?.label ?: "Happy"
        
        // Calculate consistency (percentage of days with entries in the last 7 days)
        val calendar = Calendar.getInstance()
        var consistentDays = 0
        val totalDays = 7
        
        for (i in 0 until totalDays) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = dateFormat.format(calendar.time)
            
            val entriesForDay = moodEntries.filter { it.date == dateString }
            if (entriesForDay.isNotEmpty()) {
                consistentDays++
            }
            
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        val consistencyPercentage = (consistentDays.toFloat() / totalDays * 100).toInt()
        tvConsistency.text = "$consistencyPercentage%"
        
        // Update circular progress (simplified to 70% for demo)
        circularProgressBarForeground.progress = 70
    }
    
    private fun getMoodTypeFromEmoji(emoji: String): MoodType? {
        return MoodType.getAllMoods().find { it.emoji == emoji }
    }
    
    private fun deleteMoodEntry(entry: MoodEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                prefsManager.deleteMoodEntry(entry.id)
                loadMoodHistory()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun shareMoodEntry(entry: MoodEntry) {
        val shareText = "My mood: ${entry.mood.label} ${entry.emoji}"
        val formattedText = getString(R.string.share_mood_summary, shareText)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formattedText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    private fun shareTodaysMood() {
        val todayEntries = prefsManager.getTodayMoodEntries()
        if (todayEntries.isEmpty()) return
        
        val moodSummary = if (todayEntries.size == 1) {
            "${todayEntries.first().mood.label} ${todayEntries.first().emoji}"
        } else {
            val moods = todayEntries.joinToString(", ") { "${it.mood.label} ${it.emoji}" }
            "Multiple moods today: $moods"
        }
        
        val shareText = getString(R.string.share_mood_summary, moodSummary)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    private fun showListView() {
        recyclerMoodHistory.visibility = View.VISIBLE
        layoutMoodCalendar.visibility = View.GONE
    }
    
    private fun showCalendarView() {
        recyclerMoodHistory.visibility = View.GONE
        layoutMoodCalendar.visibility = View.VISIBLE
        
        // Generate calendar view
        generateCalendarView()
    }
    
    private fun generateCalendarView() {
        // Clear existing calendar content
        if (layoutMoodCalendar is ViewGroup) {
            (layoutMoodCalendar as ViewGroup).removeAllViews()
        }
        
        // Get mood entries grouped by date
        val moodEntries = prefsManager.getMoodEntries()
        if (moodEntries.isEmpty()) {
            // Show empty state for calendar view
            val emptyView = TextView(requireContext()).apply {
                text = getString(R.string.no_mood_entries_calendar)
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                setPadding(32, 64, 32, 64)
            }
            if (layoutMoodCalendar is ViewGroup) {
                (layoutMoodCalendar as ViewGroup).addView(emptyView)
            }
            return
        }
        
        // Group entries by date
        val entriesByDate = moodEntries.groupBy { it.date }
        
        // Sort dates in descending order (newest first)
        val sortedDates = entriesByDate.keys.sortedDescending()
        
        // Create calendar layout
        val calendarLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Add title
        val title = TextView(requireContext()).apply {
            text = getString(R.string.mood_calendar_title)
            textSize = 20f
            setTextColor(requireContext().getColor(R.color.text_primary))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
            gravity = android.view.Gravity.CENTER
        }
        calendarLayout.addView(title)
        
        // Add explanation text
        val explanation = TextView(requireContext()).apply {
            text = getString(R.string.mood_calendar_explanation)
            textSize = 14f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(0, 0, 0, 16)
            gravity = android.view.Gravity.CENTER
        }
        calendarLayout.addView(explanation)
        
        // Display entries grouped by date using new item layouts
        sortedDates.forEach { date ->
            val entries = entriesByDate[date] ?: emptyList()

            // Inflate day card
            val dayCard = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_mood_calendar_day, calendarLayout, false)
            val header = dayCard.findViewById<TextView>(R.id.tv_calendar_day_header)
            val container = dayCard.findViewById<LinearLayout>(R.id.container_calendar_entries)
            header.text = formatDateHeader(date)

            // Inflate each entry row
            entries.forEachIndexed { index, entry ->
                val row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_mood_calendar_entry, container, false)
                val tvEmoji = row.findViewById<TextView>(R.id.tv_entry_emoji)
                val tvLabel = row.findViewById<TextView>(R.id.tv_entry_label)
                val tvTime = row.findViewById<TextView>(R.id.tv_entry_time)
                tvEmoji.text = entry.emoji
                tvLabel.text = entry.mood.label
                tvTime.text = formatTime(entry.timestamp)
                container.addView(row)

                if (index < entries.size - 1) {
                    // Divider
                    val divider = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                        ).apply { setMargins(0, 8, 0, 8) }
                        setBackgroundColor(requireContext().getColor(R.color.divider_color))
                    }
                    container.addView(divider)
                }
            }

            calendarLayout.addView(dayCard)
        }
        
        // Add to calendar layout
        if (layoutMoodCalendar is ViewGroup) {
            (layoutMoodCalendar as ViewGroup).addView(calendarLayout)
        }
    }
    
    private fun formatDateHeader(dateString: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString) ?: return dateString
            val today = Date()
            val todayString = dateFormat.format(today)
            
            val displayFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            val formattedDate = displayFormat.format(date)
            
            // Add "Today" indicator if this is today's date
            if (dateString == todayString) {
                "$formattedDate (Today)"
            } else {
                formattedDate
            }
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun formatTime(date: Date): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(date)
    }
    
    private fun showMoodStatisticsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_mood_statistics, null)

        val chart = dialogView.findViewById<LinearLayout>(R.id.chart_weekly_mood)
        val tvAvgEmoji = dialogView.findViewById<TextView>(R.id.tv_avg_emoji)
        val tvAvgLabel = dialogView.findViewById<TextView>(R.id.tv_avg_label)
        val tvTotalEntries = dialogView.findViewById<TextView>(R.id.tv_total_entries_dialog)
        val tvMostFrequent = dialogView.findViewById<TextView>(R.id.tv_most_frequent_dialog)
        val tvConsistency = dialogView.findViewById<TextView>(R.id.tv_consistency_dialog)

        val stats = moodStatsHelper.calculateStats()
        moodStatsHelper.bindStatsToViews(
            stats,
            tvAvgEmoji,
            tvAvgLabel,
            tvTotalEntries,
            tvMostFrequent,
            tvConsistency
        )
        moodStatsHelper.updateWeeklyMoodChart(chart)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.mood_title))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}