package com.example.hydralife.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hydralife.R
import com.example.hydralife.data.models.Habit
import com.example.hydralife.data.models.HabitProgress
import com.example.hydralife.data.repository.SharedPreferencesManager
import com.example.hydralife.ui.adapters.HabitsAdapter
import com.example.hydralife.ui.charts.CircularProgressChartHelper
import com.example.hydralife.ui.charts.MotivationalQuotesHelper
import com.example.hydralife.ui.charts.HabitStatisticsHelper
import com.example.hydralife.ui.charts.QuickHabitActionsHelper
import com.example.hydralife.ui.charts.StreakCounterHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

/**
 * Enhanced Fragment for displaying and managing daily habits with modern UI
 */
class HabitsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddHabit: ExtendedFloatingActionButton
    private lateinit var layoutEmptyState: View
    private lateinit var tvHabitCount: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvDate: TextView
    private lateinit var dateContainer: LinearLayout
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var enhancedHabitsAdapter: HabitsAdapter
    private lateinit var circularProgressChartHelper: CircularProgressChartHelper
    private lateinit var streakCounterHelper: StreakCounterHelper
    private lateinit var quickHabitActionsHelper: QuickHabitActionsHelper
    private lateinit var motivationalQuotesHelper: MotivationalQuotesHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val dayOfMonthFormat = SimpleDateFormat("dd", Locale.getDefault())
    
    // UI elements for enhanced features
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var circularProgressForeground: ProgressBar
    private lateinit var tvProgressPercentage: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var tvStreakCount: TextView
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var quickAddCard: CardView
    private lateinit var viewStatsCard: CardView
    private lateinit var motivationCard: CardView
    
    // Current filter
    private var currentCategoryFilter = Habit.CATEGORY_ALL
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        setupGreeting()
        updateCurrentDate()
        loadUserProfile()
        loadHabits()
        populateDateSelector()
        setupCategoryFilter()
        setupQuickActions()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        // Initialize standard views
        recyclerView = view.findViewById(R.id.recycler_habits)
        fabAddHabit = view.findViewById(R.id.fab_add_habit)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        tvHabitCount = view.findViewById(R.id.tv_habit_count)
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvDate = view.findViewById(R.id.tv_date)
        dateContainer = view.findViewById(R.id.dateContainer)
        
        // Initialize enhanced UI elements
        circularProgressBar = view.findViewById(R.id.circular_progress_bar)
        circularProgressForeground = view.findViewById(R.id.circular_progress_foreground)
        tvProgressPercentage = view.findViewById(R.id.tv_progress_percentage)
        tvProgressText = view.findViewById(R.id.tv_progress_text)
        tvStreakCount = view.findViewById(R.id.tv_streak_count)
        chipGroupCategories = view.findViewById(R.id.chip_group_categories)
        quickAddCard = view.findViewById(R.id.card_quick_add)
        viewStatsCard = view.findViewById(R.id.card_view_stats)
        motivationCard = view.findViewById(R.id.card_motivation)
        
        // Initialize helpers
        circularProgressChartHelper = CircularProgressChartHelper(
            circularProgressBar,
            circularProgressForeground,
            tvProgressPercentage,
            tvProgressText
        )
        
        streakCounterHelper = StreakCounterHelper(tvStreakCount, prefsManager)
        
        quickHabitActionsHelper = QuickHabitActionsHelper(
            prefsManager,
            onHabitAdded = { showAddHabitDialog() },
            onStatsViewed = { showStatisticsDashboard() },
            onMotivationViewed = { showMotivationalQuote() }
        )
        
        motivationalQuotesHelper = MotivationalQuotesHelper()
    }

    private fun setupGreeting() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            currentHour < 12 -> "Morning"
            currentHour < 17 -> "Afternoon"
            else -> "Evening"
        }
        
        // Get user's name from SharedPreferences or default to "Friend"
        val userName = prefsManager.getUserName() ?: "Friend"
        tvGreeting.text = "$greeting, $userName 👋"
    }

    private fun updateCurrentDate() {
        val currentDate = Date()
        tvDate.text = displayDateFormat.format(currentDate)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadHabits()
        setupGreeting()
        updateCurrentDate()
        loadUserProfile()
        populateDateSelector()
    }
    
    private fun populateDateSelector() {
        dateContainer.removeAllViews()
        
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        // Show 14 days (7 previous days, today, and 6 next days)
        for (i in -7..6) {
            calendar.timeInMillis = today
            calendar.add(Calendar.DAY_OF_YEAR, i)
            
            val dateView = createDayView(calendar.time)
            dateContainer.addView(dateView)
        }
    }
    
    private fun createDayView(date: Date): View {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_date_selector_enhanced, null)
        
        val dayOfWeekText = view.findViewById<TextView>(R.id.tv_day_of_week)
        val dayOfMonthText = view.findViewById<TextView>(R.id.tv_day_of_month)
        val llCompletionIndicators = view.findViewById<LinearLayout>(R.id.ll_completion_indicators)
        val vIndicator1 = view.findViewById<View>(R.id.v_indicator_1)
        val vIndicator2 = view.findViewById<View>(R.id.v_indicator_2)
        val vIndicator3 = view.findViewById<View>(R.id.v_indicator_3)
        
        dayOfWeekText.text = dayOfWeekFormat.format(date)
        dayOfMonthText.text = dayOfMonthFormat.format(date)
        
        // Highlight today
        val isToday = isSameDay(date, Date())
        if (isToday) {
            dayOfMonthText.setBackgroundResource(R.color.primary)
            dayOfMonthText.setTextColor(requireContext().getColor(R.color.white))
        } else {
            dayOfMonthText.setBackgroundResource(R.color.white)
            dayOfMonthText.setTextColor(requireContext().getColor(R.color.text_primary))
        }
        
        // Show completion indicators for this date
        val dateString = dateFormat.format(date)
        val habits = prefsManager.getHabits()
        // This can be heavy if called many times; reduce repeated JSON parses by using today's progress map where possible
        val completedHabits = habits.count { habit ->
            val progress = prefsManager.getHabitProgressForDay(habit.id, dateString)
            progress?.isCompleted ?: false
        }
        
        // Show indicators based on completion rate
        when {
            completedHabits >= 3 -> {
                vIndicator1.visibility = View.VISIBLE
                vIndicator2.visibility = View.VISIBLE
                vIndicator3.visibility = View.VISIBLE
            }
            completedHabits >= 2 -> {
                vIndicator1.visibility = View.VISIBLE
                vIndicator2.visibility = View.VISIBLE
                vIndicator3.visibility = View.GONE
            }
            completedHabits >= 1 -> {
                vIndicator1.visibility = View.VISIBLE
                vIndicator2.visibility = View.GONE
                vIndicator3.visibility = View.GONE
            }
            else -> {
                vIndicator1.visibility = View.GONE
                vIndicator2.visibility = View.GONE
                vIndicator3.visibility = View.GONE
            }
        }
        
        // Set click listener
        view.setOnClickListener {
            // Handle date selection
            loadHabitsForDate(dateFormat.format(date))
            
            // Update UI to show selected date
            populateDateSelector() // Refresh to show new selection
        }
        
        return view
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    private fun loadHabitsForDate(date: String) {
        // For now, reuse current list and update metrics. Hook here for date-specific data if needed.
        loadHabits()
    }

    private fun loadUserProfile() {
        // Get profile image path
        val profilePicPath = prefsManager.getUserProfilePicPath()
        val ivProfile = view?.findViewById<ShapeableImageView>(R.id.iv_profile)
        
        // Load profile image if exists, otherwise use default
        if (profilePicPath != null) {
            val uri = android.net.Uri.parse(profilePicPath)
            val resolver = requireContext().contentResolver
            val hasPermission = resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
            if (hasPermission) {
                try {
                    ivProfile?.setImageURI(uri)
                } catch (_: Exception) {
                    ivProfile?.setImageResource(R.drawable.avatar)
                }
            } else {
                // Permission likely lost (e.g., Photo Picker ephemeral URI) – fall back
                ivProfile?.setImageResource(R.drawable.avatar)
            }
        } else {
            ivProfile?.setImageResource(R.drawable.avatar)
        }
        
        // Set click listener for profile image
        ivProfile?.setOnClickListener {
            showProfileImagePicker()
        }
    }
    
    private fun showProfileImagePicker() {
        // Use ACTION_OPEN_DOCUMENT so we can take persistable read permission to the image
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    // Persist read permission for long-term access
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) { /* ignore if not supported */ }

                // Save the image URI
                prefsManager.setUserProfilePicPath(uri.toString())
                // Update the profile image
                try {
                    view?.findViewById<ShapeableImageView>(R.id.iv_profile)?.setImageURI(uri)
                } catch (_: Exception) { /* ignore display errors */ }
            }
        }
    }
    
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    private fun setupRecyclerView() {
        enhancedHabitsAdapter = HabitsAdapter(
            onHabitClick = { habit -> editHabit(habit) },
            onProgressClick = { habit, progress -> toggleHabitProgress(habit, progress) },
            onDeleteClick = { habit -> deleteHabit(habit) },
            onShareClick = { habit -> shareHabitProgress(habit) },
            onEditClick = { habit -> editHabit(habit) }
        )
        
        // Use different layout managers based on screen size
        val layoutManager = if (isTablet()) {
            androidx.recyclerview.widget.GridLayoutManager(context, 2)
        } else {
            LinearLayoutManager(context)
        }
        
        recyclerView.apply {
            this.layoutManager = layoutManager
            adapter = enhancedHabitsAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupClickListeners() {
        fabAddHabit.setOnClickListener {
            addNewHabit()
        }
        
        // Setup empty state button
        view?.findViewById<Button>(R.id.btn_add_first_habit)?.setOnClickListener {
            addNewHabit()
        }
    }
    
    private fun setupCategoryFilter() {
        // Set up chip group listener
        chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = chipGroupCategories.findViewById<Chip>(checkedIds[0])
                currentCategoryFilter = chip.text.toString()
                loadHabits()
            }
        }
    }
    
    private fun setupQuickActions() {
        quickHabitActionsHelper.setupQuickActionButtons(
            quickAddCard,
            viewStatsCard,
            motivationCard
        )
    }
    
    private fun loadHabits() {
        // Offload SharedPreferences JSON parsing to background thread
        viewLifecycleOwner.lifecycleScope.launch {
            val today = dateFormat.format(Date())
            val (allHabits, allProgress) = withContext(Dispatchers.IO) {
                Pair(prefsManager.getHabits(), prefsManager.getHabitProgress())
            }
            
            // Apply category filter
            val filteredHabits = if (currentCategoryFilter == Habit.CATEGORY_ALL) {
                allHabits
            } else {
                allHabits.filter { it.category == currentCategoryFilter }
            }
            
            // Update habit count
            tvHabitCount.text = "${filteredHabits.size} ${if (filteredHabits.size == 1) "habit" else "habits"}"
            
            if (filteredHabits.isEmpty()) {
                recyclerView.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
                circularProgressChartHelper.updateProgress(0, 0)
                streakCounterHelper.updateStreakCounter(emptyList())
                return@launch
            }
            
            recyclerView.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            
            // Build a map for today's progress to avoid repeated parsing
            val todayProgressByHabitId = allProgress
                .asSequence()
                .filter { it.date == today }
                .associateBy { it.habitId }
            
            val habitsWithProgress = filteredHabits.map { habit ->
                val progress = todayProgressByHabitId[habit.id] ?: HabitProgress(habit.id, today)
                Pair(habit, progress)
            }
            
            val completedCount = habitsWithProgress.count { it.second.isCompleted }
            val totalCount = filteredHabits.size
            
            circularProgressChartHelper.updateProgress(completedCount, totalCount)
            streakCounterHelper.updateStreakCounter(filteredHabits)
            enhancedHabitsAdapter.updateHabits(habitsWithProgress)
        }
    }
    
    private fun addNewHabit() {
        showAddHabitDialog()
    }
    
    private fun editHabit(habit: Habit) {
        showEditHabitDialog(habit)
    }
    
    private fun toggleHabitProgress(habit: Habit, currentProgress: HabitProgress) {
        val today = dateFormat.format(Date())
        val newProgress = if (currentProgress.isCompleted) {
            // Mark as incomplete
            currentProgress.copy(
                isCompleted = false,
                currentValue = 0,
                completionTime = null
            )
        } else {
            // Mark as complete
            currentProgress.copy(
                isCompleted = true,
                currentValue = habit.targetValue,
                completionTime = Date()
            )
        }
        
        prefsManager.saveHabitProgressForDay(habit.id, today, newProgress)
        loadHabits() // Refresh the list
    }
    
    private fun deleteHabit(habit: Habit) {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_habit))
            .setMessage("Are you sure you want to delete \"${habit.name}\"?")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                prefsManager.deleteHabit(habit.id)
                loadHabits()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun shareHabitProgress(habit: Habit) {
        val today = dateFormat.format(Date())
        val progress = prefsManager.getHabitProgressForDay(habit.id, today)
        val progressText = if (progress?.isCompleted == true) {
            "Completed: ${habit.name}"
        } else {
            "Working on: ${habit.name}"
        }
        
        val shareText = getString(R.string.share_habit_progress, progressText)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)
        
        val etHabitName = dialogView.findViewById<TextInputEditText>(R.id.et_habit_name)
        val etHabitDescription = dialogView.findViewById<TextInputEditText>(R.id.et_habit_description)
        val etTargetValue = dialogView.findViewById<TextInputEditText>(R.id.et_target_value)
        val spinnerUnit = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_unit)
        val spinnerCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_category)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chip_group_units)
        
        // Setup unit selector
        val units = arrayOf("times", "minutes", "hours", "glasses", "pages", "km")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        spinnerUnit.setAdapter(unitAdapter)
        spinnerUnit.setText("times", false)
        
        // Setup category selector
        val categories = Habit.CATEGORIES.filter { it != Habit.CATEGORY_ALL }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(categoryAdapter)
        spinnerCategory.setText(Habit.CATEGORY_PERSONAL, false)
        
        // Setup unit selector with chips
        setupUnitSelector(dialogView, spinnerUnit, chipGroup)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_habit))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = etHabitName.text?.toString()?.trim()
                val description = etHabitDescription.text?.toString()?.trim() ?: ""
                val targetText = etTargetValue.text?.toString()?.trim()
                val unit = spinnerUnit.text?.toString() ?: "times"
                val category = spinnerCategory.text?.toString() ?: Habit.CATEGORY_PERSONAL
                
                if (!name.isNullOrBlank() && !targetText.isNullOrBlank()) {
                    try {
                        val target = targetText.toInt()
                        if (target > 0) {
                            val habit = Habit(
                                name = name,
                                description = description,
                                targetValue = target,
                                unit = unit,
                                category = category
                            )
                            prefsManager.saveHabit(habit)
                            loadHabits()
                            
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Habit added successfully!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Target value must be greater than 0",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: NumberFormatException) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please enter a valid target number",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please fill in all required fields",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_habit, null)
        
        val etHabitName = dialogView.findViewById<TextInputEditText>(R.id.et_habit_name)
        val etHabitDescription = dialogView.findViewById<TextInputEditText>(R.id.et_habit_description)
        val etTargetValue = dialogView.findViewById<TextInputEditText>(R.id.et_target_value)
        val spinnerUnit = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_unit)
        val spinnerCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spinner_category)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chip_group_units)
        
        // Pre-fill with current values
        etHabitName.setText(habit.name)
        etHabitDescription.setText(habit.description)
        etTargetValue.setText(habit.targetValue.toString())
        
        // Setup unit selector
        val units = arrayOf("times", "minutes", "hours", "glasses", "pages", "km")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        spinnerUnit.setAdapter(unitAdapter)
        spinnerUnit.setText(habit.unit, false)
        
        // Setup category selector
        val categories = Habit.CATEGORIES.filter { it != Habit.CATEGORY_ALL }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(categoryAdapter)
        spinnerCategory.setText(habit.category, false)
        
        // Setup unit selector with chips
        setupUnitSelector(dialogView, spinnerUnit, chipGroup)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_habit))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = etHabitName.text?.toString()?.trim()
                val description = etHabitDescription.text?.toString()?.trim() ?: ""
                val targetText = etTargetValue.text?.toString()?.trim()
                val unit = spinnerUnit.text?.toString() ?: "times"
                val category = spinnerCategory.text?.toString() ?: Habit.CATEGORY_PERSONAL
                
                if (!name.isNullOrBlank() && !targetText.isNullOrBlank()) {
                    try {
                        val target = targetText.toInt()
                        if (target > 0) {
                            val updatedHabit = habit.copy(
                                name = name,
                                description = description,
                                targetValue = target,
                                unit = unit,
                                category = category
                            )
                            prefsManager.saveHabit(updatedHabit)
                            loadHabits()
                            
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Habit updated successfully!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Target value must be greater than 0",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: NumberFormatException) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please enter a valid target number",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please fill in all required fields",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun setupUnitSelector(dialogView: View, spinnerUnit: AutoCompleteTextView, chipGroup: ChipGroup) {
        // Set up chip group click listeners
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.setOnClickListener {
                spinnerUnit.setText(chip.text.toString(), false)
                chipGroup.visibility = View.GONE
            }
        }
        
        // Set up spinner click to show chips
        spinnerUnit.setOnClickListener {
            chipGroup.visibility = if (chipGroup.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }
    
    private fun showStatisticsDashboard() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_statistics_dashboard, null)
        
        val tvTotalHabits = dialogView.findViewById<TextView>(R.id.tv_total_habits)
        val tvCompletionRate = dialogView.findViewById<TextView>(R.id.tv_completion_rate)
        val tvCurrentStreak = dialogView.findViewById<TextView>(R.id.tv_current_streak)
        val llCategoryStats = dialogView.findViewById<LinearLayout>(R.id.ll_category_stats)
        val llWeeklyChart = dialogView.findViewById<LinearLayout>(R.id.ll_weekly_chart)
        
        val habits = prefsManager.getHabits()
        
        // Use statistics helper to populate all stats blocks
        val statsHelper = HabitStatisticsHelper(prefsManager)
        statsHelper.updateOverallStats(habits, tvTotalHabits, tvCompletionRate, tvCurrentStreak)
        statsHelper.updateCategoryBreakdown(habits, llCategoryStats)
        statsHelper.updateWeeklyProgressChart(habits, llWeeklyChart)
        
        // Show dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }
    
    private fun showMotivationalQuote() {
        val quote = motivationalQuotesHelper.getRandomMotivationalQuote()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Motivation")
            .setMessage(quote)
            .setPositiveButton("Thanks!", null)
            .show()
    }
    
    private fun isTablet(): Boolean {
        val configuration = resources.configuration
        return configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
}