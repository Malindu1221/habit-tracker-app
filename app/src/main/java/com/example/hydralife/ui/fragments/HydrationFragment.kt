package com.example.hydralife.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hydralife.MainActivity
import com.example.hydralife.R
import com.example.hydralife.data.models.HydrationIntake
import com.example.hydralife.data.models.HydrationSettings
import com.example.hydralife.data.repository.SharedPreferencesManager
import com.example.hydralife.receivers.HydrationAlarmScheduler
import com.example.hydralife.ui.adapters.HydrationHistoryAdapter
import com.example.hydralife.ui.charts.HydrationStatisticsHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for hydration tracking and reminders
 */
class HydrationFragment : Fragment() {
    
    private lateinit var tvDailyGoal: TextView
    private lateinit var tvCurrentIntake: TextView
    private lateinit var recyclerHydrationHistory: RecyclerView
    
    // New views for enhanced UI
    private lateinit var tvIntakePercentage: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvEntriesCount: TextView
    private lateinit var circularProgressBarBackground: ProgressBar
    private lateinit var circularProgressBarForeground: ProgressBar
    private lateinit var cardAddWater: MaterialCardView
    private lateinit var cardSetReminder: MaterialCardView
    private lateinit var cardViewStats: MaterialCardView
    private lateinit var cardResetToday: MaterialCardView
    private lateinit var btnQuickAdd250: MaterialButton
    private lateinit var btnQuickAdd330: MaterialButton
    private lateinit var btnQuickAdd500: MaterialButton
    private lateinit var btnQuickAdd750: MaterialButton
    private lateinit var btnQuickAdd1000: MaterialButton
    private lateinit var btnCustomAmount: MaterialButton
    private lateinit var etCustomAmount: TextInputEditText
    private lateinit var btnAddCustom: MaterialButton
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var hydrationHistoryAdapter: HydrationHistoryAdapter
    private lateinit var hydrationStatsHelper: HydrationStatisticsHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupHydrationHistory()
        setupClickListeners()
        updateHydrationDisplay()
    }
    
    override fun onResume() {
        super.onResume()
        updateHydrationDisplay()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        hydrationStatsHelper = HydrationStatisticsHelper(prefsManager)
        
        // Views for enhanced UI
        tvDailyGoal = view.findViewById(R.id.tv_daily_goal)
        tvCurrentIntake = view.findViewById(R.id.tv_current_intake)
        recyclerHydrationHistory = view.findViewById(R.id.recycler_hydration_history)
        
        // New views for enhanced UI
        tvIntakePercentage = view.findViewById(R.id.tv_intake_percentage)
        tvRemaining = view.findViewById(R.id.tv_remaining)
        tvEntriesCount = view.findViewById(R.id.tv_entries_count)
        circularProgressBarBackground = view.findViewById(R.id.circular_progress_bar_background)
        circularProgressBarForeground = view.findViewById(R.id.circular_progress_bar_foreground)
        cardAddWater = view.findViewById(R.id.card_add_water)
        cardSetReminder = view.findViewById(R.id.card_set_reminder)
        cardViewStats = view.findViewById(R.id.card_view_stats)
        cardResetToday = view.findViewById(R.id.card_reset_today)
        
        // Quick add buttons
        btnQuickAdd250 = view.findViewById(R.id.btn_quick_add_250)
        btnQuickAdd330 = view.findViewById(R.id.btn_quick_add_330)
        btnQuickAdd500 = view.findViewById(R.id.btn_quick_add_500)
        btnQuickAdd750 = view.findViewById(R.id.btn_quick_add_750)
        btnQuickAdd1000 = view.findViewById(R.id.btn_quick_add_1000)
        btnCustomAmount = view.findViewById(R.id.btn_custom_amount)
        
        // Custom amount views
        etCustomAmount = view.findViewById(R.id.et_custom_amount)
        btnAddCustom = view.findViewById(R.id.btn_add_custom)
    }
    
    private fun setupHydrationHistory() {
        hydrationHistoryAdapter = HydrationHistoryAdapter()
        
        recyclerHydrationHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = hydrationHistoryAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupClickListeners() {
        // Card click listeners
        cardAddWater.setOnClickListener {
            showAddWaterDialog()
        }
        
        cardSetReminder.setOnClickListener {
            showReminderSettingsDialog()
        }
        
        cardViewStats.setOnClickListener {
            showHydrationStatisticsDialog()
        }

        cardResetToday.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Today's Hydration")
                .setMessage("This will remove all hydration entries for today. Continue?")
                .setPositiveButton("Reset") { _, _ ->
                    prefsManager.resetTodayHydration()
                    updateHydrationDisplay()
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Today's hydration reset",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        
        // Quick add buttons
        btnQuickAdd250.setOnClickListener { addWaterIntake(250) }
        btnQuickAdd330.setOnClickListener { addWaterIntake(330) }
        btnQuickAdd500.setOnClickListener { addWaterIntake(500) }
        btnQuickAdd750.setOnClickListener { addWaterIntake(750) }
        btnQuickAdd1000.setOnClickListener { addWaterIntake(1000) }
        
        // Custom amount
        btnCustomAmount.setOnClickListener {
            showAddWaterDialog()
        }
        
        btnAddCustom.setOnClickListener {
            val amountText = etCustomAmount.text?.toString()
            if (!amountText.isNullOrBlank()) {
                try {
                    val amount = amountText.toInt()
                    if (amount > 0) {
                        addWaterIntake(amount)
                        etCustomAmount.text?.clear()
                    } else {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please enter a valid amount",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please enter a valid number",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please enter an amount",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun updateHydrationDisplay() {
        val settings = prefsManager.getHydrationSettings()
        val todayIntake = prefsManager.getTodayTotalHydration()
        val todayIntakeList = prefsManager.getTodayHydrationIntake()
        
        // Update goal and current intake
        tvDailyGoal.text = "of ${settings.dailyGoalMl} ${getString(R.string.ml_unit)}"
        tvCurrentIntake.text = "$todayIntake ${getString(R.string.ml_unit)}"
        
        // Update progress
        val progressPercentage = if (settings.dailyGoalMl > 0) {
            ((todayIntake.toFloat() / settings.dailyGoalMl.toFloat()) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }
        
        // Update circular progress bar
        circularProgressBarForeground.progress = progressPercentage
        
        // Update new stats views
        tvIntakePercentage.text = "$progressPercentage%"
        tvRemaining.text = "${settings.dailyGoalMl - todayIntake} ml"
        tvEntriesCount.text = todayIntakeList.size.toString()
        
        // Update history
        val allIntakes = prefsManager.getHydrationIntake().take(10) // Show last 10 entries
        hydrationHistoryAdapter.updateIntakes(allIntakes)
        
        // Check if goal is reached
        if (progressPercentage >= 100) {
            // Show congratulations
            android.widget.Toast.makeText(
                requireContext(),
                getString(R.string.goal_reached),
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showAddWaterDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_water, null)
        
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.et_water_amount)
        
        // Setup preset buttons
        val btnPreset200 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_200)
        val btnPreset250 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_250)
        val btnPreset300 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_300)
        val btnPreset330 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_330)
        val btnPreset500 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_500)
        val btnPreset750 = dialogView.findViewById<MaterialButton>(R.id.btn_preset_750)
        
        // Set up click listeners for preset buttons
        btnPreset200.setOnClickListener { etAmount.setText("200") }
        btnPreset250.setOnClickListener { etAmount.setText("250") }
        btnPreset300.setOnClickListener { etAmount.setText("300") }
        btnPreset330.setOnClickListener { etAmount.setText("330") }
        btnPreset500.setOnClickListener { etAmount.setText("500") }
        btnPreset750.setOnClickListener { etAmount.setText("750") }
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_water))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val amountText = etAmount.text?.toString()
                if (!amountText.isNullOrBlank()) {
                    try {
                        val amount = amountText.toInt()
                        if (amount > 0) {
                            addWaterIntake(amount)
                        } else {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Please enter a valid amount",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: NumberFormatException) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please enter a valid number",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Please enter an amount",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun addWaterIntake(amountMl: Int) {
        val today = dateFormat.format(Date())
        val intake = HydrationIntake(
            date = today,
            amountMl = amountMl,
            timestamp = Date()
        )
        
        prefsManager.addHydrationIntake(intake)
        updateHydrationDisplay()
        
        // Check if goal is reached after adding this intake
        checkAndNotifyGoalReached()
        
        android.widget.Toast.makeText(
            requireContext(),
            "Added ${amountMl}ml of water",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun checkAndNotifyGoalReached() {
        val settings = prefsManager.getHydrationSettings()
        val todayIntake = prefsManager.getTodayTotalHydration()
        
        // Check if user just reached their goal
        if (todayIntake >= settings.dailyGoalMl) {
            sendGoalReachedNotification()
        }
    }
    
    private fun sendGoalReachedNotification() {
        // Create intent to open the app when notification is tapped
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_hydration", true)
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(),
            2, // Different request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get default alarm sound
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        // Create notification channel first
        createNotificationChannel()
        
        // Build the goal reached notification
        val notification = NotificationCompat.Builder(requireContext(), "hydration_reminders")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Goal Reached! 🎉")
            .setContentText("Congratulations! You've reached your daily hydration goal.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Great job staying hydrated! You've reached your daily water intake goal.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri) // Add alarm sound for goal reached
            .setVibrate(longArrayOf(0, 500, 250, 500)) // Vibrate pattern
            .build()
        
        // Show the notification
        try {
            NotificationManagerCompat.from(requireContext()).notify(1002, notification)
        } catch (e: SecurityException) {
            // Handle the case where notification permission is not granted
            android.widget.Toast.makeText(
                requireContext(),
                "Please enable notification permission to receive goal alerts",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_hydration_name)
            val descriptionText = getString(R.string.channel_hydration_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("hydration_reminders", name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showReminderSettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_reminder_settings, null)
        
        val etGoal = dialogView.findViewById<TextInputEditText>(R.id.et_daily_goal)
        val tvReminderTime = dialogView.findViewById<TextView>(R.id.tv_reminder_time)
        val cardNotificationPermission = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_notification_permission)
        val btnGrantPermission = dialogView.findViewById<MaterialButton>(R.id.btn_grant_permission)
        
        // Frequency preset buttons
        val btnFreq30 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_30)
        val btnFreq1 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_1)
        val btnFreq2 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_2)
        val btnFreq3 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_3)
        val btnFreq4 = dialogView.findViewById<MaterialButton>(R.id.btn_freq_4)
        val btnFreqCustom = dialogView.findViewById<MaterialButton>(R.id.btn_freq_custom)
        
        val currentSettings = prefsManager.getHydrationSettings()
        
        // Set current values
        etGoal.setText(currentSettings.dailyGoalMl.toString())
        
        // Format reminder time (now with minutes)
        val reminderTimeText = String.format("%02d:%02d", currentSettings.startTime, currentSettings.startMinute)
        tvReminderTime.text = reminderTimeText
        
        // Variable to track selected frequency
        var selectedFrequency = currentSettings.reminderIntervalMinutes
        
        // Variable to track selected time (hour and minute)
        var selectedHour = currentSettings.startTime
        var selectedMinute = currentSettings.startMinute
        
        // Set up frequency button listeners
        btnFreq30.setOnClickListener { 
            selectedFrequency = 30
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.primary_green)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq1.setOnClickListener { 
            selectedFrequency = 60
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.primary_green)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq2.setOnClickListener { 
            selectedFrequency = 120
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.primary_green)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq3.setOnClickListener { 
            selectedFrequency = 180
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.primary_green)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreq4.setOnClickListener { 
            selectedFrequency = 240
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.primary_green)
            btnFreqCustom.setStrokeColorResource(R.color.text_hint)
        }
        
        btnFreqCustom.setOnClickListener { 
            selectedFrequency = 60 // Default custom to 1 hour
            // Update button states
            btnFreq30.setStrokeColorResource(R.color.text_hint)
            btnFreq1.setStrokeColorResource(R.color.text_hint)
            btnFreq2.setStrokeColorResource(R.color.text_hint)
            btnFreq3.setStrokeColorResource(R.color.text_hint)
            btnFreq4.setStrokeColorResource(R.color.text_hint)
            btnFreqCustom.setStrokeColorResource(R.color.primary_green)
        }
        
        // Set initial selected button based on current settings
        when (currentSettings.reminderIntervalMinutes) {
            30 -> {
                selectedFrequency = 30
                btnFreq30.setStrokeColorResource(R.color.primary_green)
            }
            60 -> {
                selectedFrequency = 60
                btnFreq1.setStrokeColorResource(R.color.primary_green)
            }
            120 -> {
                selectedFrequency = 120
                btnFreq2.setStrokeColorResource(R.color.primary_green)
            }
            180 -> {
                selectedFrequency = 180
                btnFreq3.setStrokeColorResource(R.color.primary_green)
            }
            240 -> {
                selectedFrequency = 240
                btnFreq4.setStrokeColorResource(R.color.primary_green)
            }
            else -> {
                selectedFrequency = 60
                btnFreqCustom.setStrokeColorResource(R.color.primary_green)
            }
        }
        
        // Set up time picker (now with minutes)
        tvReminderTime.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    tvReminderTime.text = String.format("%02d:%02d", hour, minute)
                },
                selectedHour,
                selectedMinute,
                true
            )
            timePicker.show()
        }
        
        // Check notification permission and show/hide permission card
        if (checkNotificationPermission()) {
            cardNotificationPermission.visibility = View.GONE
        } else {
            cardNotificationPermission.visibility = View.VISIBLE
            btnGrantPermission.setOnClickListener {
                requestNotificationPermission()
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.set_reminder))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val goalText = etGoal.text?.toString()
                
                if (!goalText.isNullOrBlank()) {
                    try {
                        val newGoal = goalText.toInt()
                        
                        val newSettings = currentSettings.copy(
                            dailyGoalMl = newGoal,
                            reminderIntervalMinutes = selectedFrequency,
                            startTime = selectedHour,
                            startMinute = selectedMinute,
                            endTime = if (selectedHour + 1 > 23) 23 else selectedHour + 1 // Set end time to 1 hour after start
                        )
                        
                        prefsManager.saveHydrationSettings(newSettings)
                        
                        // Check for exact alarm permission on Android 12+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (HydrationAlarmScheduler.canScheduleExactAlarms(requireContext())) {
                                // Use the enhanced alarm scheduler with minute precision
                                HydrationAlarmScheduler.scheduleRecurringAlarmWithMinutes(
                                    requireContext(), 
                                    newSettings, 
                                    selectedMinute
                                )
                            } else {
                                // Request exact alarm permission
                                HydrationAlarmScheduler.requestExactAlarmPermission(requireContext())
                                android.widget.Toast.makeText(
                                    requireContext(),
                                    "Please grant exact alarm permission in settings",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@setPositiveButton
                            }
                        } else {
                            // For older versions, directly schedule the alarm
                            HydrationAlarmScheduler.scheduleRecurringAlarmWithMinutes(
                                requireContext(), 
                                newSettings, 
                                selectedMinute
                            )
                        }
                        
                        updateHydrationDisplay()
                        
                        // Show next alarm time
                        val nextAlarmText = HydrationAlarmScheduler.getNextAlarmTimeFormatted(requireContext())
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Settings saved. $nextAlarmText",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        
                    } catch (e: NumberFormatException) {
                        // Invalid number
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Please enter a valid number for the goal",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions are automatically granted on older versions
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }
    
    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Notification permission granted",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Notification permission denied",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun setupHydrationReminders(settings: HydrationSettings) {
        if (!settings.reminderEnabled) {
            HydrationAlarmScheduler.cancelAlarm(requireContext())
            return
        }
        
        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (HydrationAlarmScheduler.canScheduleExactAlarms(requireContext())) {
                // Use the new alarm scheduler
                HydrationAlarmScheduler.scheduleRecurringAlarm(requireContext(), settings)
            }
            // If we can't schedule exact alarms, the alarms will remain unscheduled
            // The user will need to manually enable them in the app
        } else {
            // For older versions, directly schedule the alarm
            HydrationAlarmScheduler.scheduleRecurringAlarm(requireContext(), settings)
        }
    }

    private fun showHydrationStatisticsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_hydration_statistics, null)

        val chart = dialogView.findViewById<LinearLayout>(R.id.chart_weekly_hydration)
        val tvTotalDays = dialogView.findViewById<TextView>(R.id.tv_total_days_tracked)
        val tvAvgIntake = dialogView.findViewById<TextView>(R.id.tv_average_intake)
        val tvGoalRate = dialogView.findViewById<TextView>(R.id.tv_goal_rate)
        val tvCurrentStreak = dialogView.findViewById<TextView>(R.id.tv_current_streak)
        val tvLongestStreak = dialogView.findViewById<TextView>(R.id.tv_longest_streak)

        val stats = hydrationStatsHelper.calculateStats()
        hydrationStatsHelper.bindStatsToViews(
            stats,
            tvTotalDays,
            tvAvgIntake,
            tvGoalRate,
            tvCurrentStreak,
            tvLongestStreak
        )
        hydrationStatsHelper.updateWeeklyProgressChart(chart)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.hydration_title))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}