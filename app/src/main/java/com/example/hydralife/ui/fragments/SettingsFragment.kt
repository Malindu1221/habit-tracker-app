package com.example.hydralife.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hydralife.MainActivity
import com.example.hydralife.R
import com.example.hydralife.data.models.HydrationSettings
import com.example.hydralife.data.repository.SharedPreferencesManager
import com.example.hydralife.ui.auth.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for app settings and preferences
 */
class SettingsFragment : Fragment() {
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var cardHydrationSettings: MaterialCardView
    private lateinit var cardMoodTracking: MaterialCardView
    private lateinit var cardAppInfo: MaterialCardView
    private lateinit var cardLogout: MaterialCardView
    private lateinit var switchNotifications: MaterialSwitch
    private lateinit var switchGeneral: MaterialSwitch
    private lateinit var tvAppVersion: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var cardQuickHabits: MaterialCardView
    private lateinit var cardQuickMood: MaterialCardView
    private lateinit var cardQuickHydration: MaterialCardView
    private lateinit var circularProgressBarForeground: ProgressBar
    private lateinit var tvCompletionPercentage: TextView
    private lateinit var tvHabitsTracked: TextView
    private lateinit var tvMoodEntries: TextView
    private lateinit var tvHydrationIntake: TextView
    private lateinit var ivProfileDetails: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupClickListeners()
        loadSettings()
        updateStats()
        loadUserProfile()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        cardHydrationSettings = view.findViewById(R.id.card_hydration_settings)
        cardMoodTracking = view.findViewById(R.id.card_mood_tracking)
        cardAppInfo = view.findViewById(R.id.card_app_info)
        cardLogout = view.findViewById(R.id.card_logout)
        switchNotifications = view.findViewById(R.id.switch_notifications)
        switchGeneral = view.findViewById(R.id.switch_general)
        tvAppVersion = view.findViewById(R.id.tv_app_version)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        cardQuickHabits = view.findViewById(R.id.card_quick_habits)
        cardQuickMood = view.findViewById(R.id.card_quick_mood)
        cardQuickHydration = view.findViewById(R.id.card_quick_hydration)
        circularProgressBarForeground = view.findViewById(R.id.circular_progress_bar_foreground)
        tvCompletionPercentage = view.findViewById(R.id.tv_completion_percentage)
        tvHabitsTracked = view.findViewById(R.id.tv_habits_tracked)
        tvMoodEntries = view.findViewById(R.id.tv_mood_entries)
        tvHydrationIntake = view.findViewById(R.id.tv_hydration_intake)
        ivProfileDetails = view.findViewById(R.id.iv_profile_details)
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserEmail = view.findViewById(R.id.tv_user_email)
    }
    
    private fun setupClickListeners() {
        cardHydrationSettings.setOnClickListener {
            // Open hydration settings (could navigate to hydration fragment)
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.nav_hydration
        }
        
        cardMoodTracking.setOnClickListener {
            // Open mood tracking (could navigate to mood fragment)
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.nav_mood
        }
        
        cardAppInfo.setOnClickListener {
            showAboutDialog()
        }
        
        cardLogout.setOnClickListener {
            showLogoutConfirmation()
        }
        
        btnEditProfile.setOnClickListener {
            // TODO: Implement profile editing functionality
            android.widget.Toast.makeText(
                requireContext(),
                "Profile editing feature coming soon",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationSettings(isChecked)
        }
        
        switchGeneral.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implement general notification settings
            android.widget.Toast.makeText(
                requireContext(),
                "General notifications ${if (isChecked) "enabled" else "disabled"}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        // Quick action cards
        cardQuickHabits.setOnClickListener {
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.nav_habits
        }
        
        cardQuickMood.setOnClickListener {
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.nav_mood
        }
        
        cardQuickHydration.setOnClickListener {
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?.selectedItemId = R.id.nav_hydration
        }
    }
    
    private fun loadSettings() {
        val hydrationSettings = prefsManager.getHydrationSettings()
        switchNotifications.isChecked = hydrationSettings.reminderEnabled
        tvAppVersion.text = getString(R.string.version_info)
    }
    
    private fun updateStats() {
        // Get actual stats from repository
        val habits = prefsManager.getHabits()
        val habitsTracked = habits.size
        
        val moodEntries = prefsManager.getTodayMoodEntries().size
        
        val hydrationIntake = prefsManager.getTodayTotalHydration()
        val hydrationIntakeFormatted = if (hydrationIntake >= 1000) {
            "${String.format("%.1f", hydrationIntake / 1000.0)}L"
        } else {
            "${hydrationIntake}ml"
        }
        
        // Calculate completion percentage
        val completionPercentage = calculateOverallCompletion(habits)
        
        // Update UI
        tvHabitsTracked.text = habitsTracked.toString()
        tvMoodEntries.text = moodEntries.toString()
        tvHydrationIntake.text = hydrationIntakeFormatted
        tvCompletionPercentage.text = "${completionPercentage}%"
        circularProgressBarForeground.progress = completionPercentage
    }
    
    private fun calculateOverallCompletion(habits: List<com.example.hydralife.data.models.Habit>): Int {
        if (habits.isEmpty()) return 0
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var totalProgress = 0
        
        for (habit in habits) {
            val progress = prefsManager.getHabitProgressForDay(habit.id, today)
            val progressPercentage = progress?.getProgressPercentage(habit.targetValue) ?: 0
            totalProgress += progressPercentage
        }
        
        return (totalProgress / habits.size).coerceAtMost(100)
    }
    
    private fun loadUserProfile() {
        // Load user profile information
        val userName = prefsManager.getUserName() ?: "User"
        val userEmail = "user@example.com" // This would come from actual user data
        
        tvUserName.text = userName
        tvUserEmail.text = userEmail
        
        // TODO: Load actual profile picture if available
    }
    
    private fun updateNotificationSettings(enabled: Boolean) {
        val currentSettings = prefsManager.getHydrationSettings()
        val newSettings = currentSettings.copy(reminderEnabled = enabled)
        prefsManager.saveHydrationSettings(newSettings)
        
        if (enabled) {
            // Enable notifications
            android.widget.Toast.makeText(
                requireContext(),
                "Notifications enabled",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            // Disable notifications
            androidx.work.WorkManager.getInstance(requireContext())
                .cancelUniqueWork("hydration_reminder")
            android.widget.Toast.makeText(
                requireContext(),
                "Notifications disabled",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.about))
            .setMessage(getString(R.string.about_description))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
    
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun logout() {
        // Clear user login status
        prefsManager.setUserLoggedIn(false)
        
        // Navigate to login screen
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}