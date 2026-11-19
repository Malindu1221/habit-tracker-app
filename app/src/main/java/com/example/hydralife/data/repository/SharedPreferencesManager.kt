package com.example.hydralife.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.hydralife.data.models.*
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject
import com.example.hydralife.data.models.MoodType

/**
 * Manager class for handling SharedPreferences operations
 * Provides methods to store and retrieve all app data
 */
class SharedPreferencesManager(context: Context) {
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        private const val PREFS_NAME = "wellnesmate_prefs"
        
        // Keys for different data types
        private const val KEY_HABITS = "habits"
        private const val KEY_HABIT_PROGRESS = "habit_progress"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_SETTINGS = "hydration_settings"
        private const val KEY_HYDRATION_INTAKE = "hydration_intake"
        private const val KEY_APP_SETTINGS = "app_settings"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LAST_BACKUP = "last_backup"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PROFILE_PIC = "user_profile_pic"
        private const val KEY_USER_METRICS = "user_metrics"
        
        @Volatile
        private var INSTANCE: SharedPreferencesManager? = null
        
        fun getInstance(context: Context): SharedPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // HABIT MANAGEMENT
    
    fun saveHabits(habits: List<Habit>) {
        val jsonArray = JSONArray()
        habits.forEach { habit ->
            val jsonObject = JSONObject().apply {
                put("id", habit.id)
                put("name", habit.name)
                put("description", habit.description)
                put("targetValue", habit.targetValue)
                put("unit", habit.unit)
                put("category", habit.category)
                put("createdDate", habit.createdDate.time)
            }
            jsonArray.put(jsonObject)
        }
        sharedPrefs.edit().putString(KEY_HABITS, jsonArray.toString()).apply()
    }
    
    fun getHabits(): List<Habit> {
        val json = sharedPrefs.getString(KEY_HABITS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val habits = mutableListOf<Habit>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val habit = Habit(
                    id = jsonObject.getString("id"),
                    name = jsonObject.getString("name"),
                    description = jsonObject.getString("description"),
                    targetValue = jsonObject.getInt("targetValue"),
                    unit = jsonObject.getString("unit"),
                    category = jsonObject.getString("category"),
                    createdDate = Date(jsonObject.getLong("createdDate"))
                )
                habits.add(habit)
            }
            habits
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        val existingIndex = habits.indexOfFirst { it.id == habit.id }
        if (existingIndex >= 0) {
            habits[existingIndex] = habit
        } else {
            habits.add(habit)
        }
        saveHabits(habits)
    }
    
    fun deleteHabit(habitId: String) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
        
        // Also delete related progress data
        deleteHabitProgress(habitId)
    }
    
    // HABIT PROGRESS MANAGEMENT
    
    fun saveHabitProgress(progress: List<HabitProgress>) {
        val jsonArray = JSONArray()
        progress.forEach { habitProgress ->
            val jsonObject = JSONObject().apply {
                put("habitId", habitProgress.habitId)
                put("date", habitProgress.date)
                put("currentValue", habitProgress.currentValue)
                put("isCompleted", habitProgress.isCompleted)
                put("completionTime", habitProgress.completionTime?.time ?: 0)
            }
            jsonArray.put(jsonObject)
        }
        sharedPrefs.edit().putString(KEY_HABIT_PROGRESS, jsonArray.toString()).apply()
    }
    
    fun getHabitProgress(): List<HabitProgress> {
        val json = sharedPrefs.getString(KEY_HABIT_PROGRESS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val progressList = mutableListOf<HabitProgress>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val completionTime = jsonObject.getLong("completionTime")
                val progress = HabitProgress(
                    habitId = jsonObject.getString("habitId"),
                    date = jsonObject.getString("date"),
                    currentValue = jsonObject.getInt("currentValue"),
                    isCompleted = jsonObject.getBoolean("isCompleted"),
                    completionTime = if (completionTime > 0) Date(completionTime) else null
                )
                progressList.add(progress)
            }
            progressList
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveHabitProgressForDay(habitId: String, date: String, progress: HabitProgress) {
        val allProgress = getHabitProgress().toMutableList()
        val existingIndex = allProgress.indexOfFirst { it.habitId == habitId && it.date == date }
        
        if (existingIndex >= 0) {
            allProgress[existingIndex] = progress
        } else {
            allProgress.add(progress)
        }
        saveHabitProgress(allProgress)
    }
    
    fun getHabitProgressForDay(habitId: String, date: String): HabitProgress? {
        return getHabitProgress().find { it.habitId == habitId && it.date == date }
    }
    
    fun getTodayHabitProgress(habitId: String): HabitProgress? {
        val today = dateFormat.format(Date())
        return getHabitProgressForDay(habitId, today)
    }
    
    private fun deleteHabitProgress(habitId: String) {
        val progress = getHabitProgress().toMutableList()
        progress.removeAll { it.habitId == habitId }
        saveHabitProgress(progress)
    }
    
    // MOOD MANAGEMENT
    
    fun saveMoodEntries(entries: List<MoodEntry>) {
        val jsonArray = JSONArray()
        entries.forEach { entry ->
            val jsonObject = JSONObject().apply {
                put("id", entry.id)
                put("mood", entry.mood.name)
                put("emoji", entry.emoji)
                put("notes", entry.notes)
                put("timestamp", entry.timestamp.time)
                put("date", entry.date)
            }
            jsonArray.put(jsonObject)
        }
        sharedPrefs.edit().putString(KEY_MOOD_ENTRIES, jsonArray.toString()).apply()
    }
    
    fun getMoodEntries(): List<MoodEntry> {
        val json = sharedPrefs.getString(KEY_MOOD_ENTRIES, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val entries = mutableListOf<MoodEntry>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val entry = MoodEntry(
                    id = jsonObject.getString("id"),
                    mood = MoodType.valueOf(jsonObject.getString("mood")),
                    emoji = jsonObject.getString("emoji"),
                    notes = jsonObject.getString("notes"),
                    timestamp = Date(jsonObject.getLong("timestamp")),
                    date = jsonObject.getString("date")
                )
                entries.add(entry)
            }
            entries
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveMoodEntry(entry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        val existingIndex = entries.indexOfFirst { it.id == entry.id }
        if (existingIndex >= 0) {
            entries[existingIndex] = entry
        } else {
            entries.add(0, entry) // Add to beginning for chronological order
        }
        saveMoodEntries(entries)
    }
    
    fun deleteMoodEntry(entryId: String) {
        val entries = getMoodEntries().toMutableList()
        entries.removeAll { it.id == entryId }
        saveMoodEntries(entries)
    }
    
    fun getTodayMoodEntries(): List<MoodEntry> {
        val today = dateFormat.format(Date())
        return getMoodEntries().filter { it.date == today }
    }
    
    // HYDRATION MANAGEMENT
    
    fun saveHydrationSettings(settings: HydrationSettings) {
        val jsonObject = JSONObject().apply {
            put("dailyGoalMl", settings.dailyGoalMl)
            put("reminderEnabled", settings.reminderEnabled)
            put("reminderIntervalMinutes", settings.reminderIntervalMinutes)
            put("startTime", settings.startTime)
            put("endTime", settings.endTime)
            put("startMinute", settings.startMinute)
        }
        sharedPrefs.edit().putString(KEY_HYDRATION_SETTINGS, jsonObject.toString()).apply()
    }
    
    fun getHydrationSettings(): HydrationSettings {
        val json = sharedPrefs.getString(KEY_HYDRATION_SETTINGS, null)
        return if (json != null) {
            try {
                val jsonObject = JSONObject(json)
                HydrationSettings(
                    dailyGoalMl = jsonObject.getInt("dailyGoalMl"),
                    reminderEnabled = jsonObject.getBoolean("reminderEnabled"),
                    reminderIntervalMinutes = jsonObject.getInt("reminderIntervalMinutes"),
                    startTime = jsonObject.getInt("startTime"),
                    endTime = jsonObject.getInt("endTime"),
                    startMinute = if (jsonObject.has("startMinute")) jsonObject.getInt("startMinute") else 0
                )
            } catch (e: Exception) {
                HydrationSettings()
            }
        } else {
            HydrationSettings()
        }
    }
    
    fun saveHydrationIntake(intake: List<HydrationIntake>) {
        val jsonArray = JSONArray()
        intake.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("amountMl", item.amountMl)
                put("date", item.date)
                put("timestamp", item.timestamp.time)
            }
            jsonArray.put(jsonObject)
        }
        sharedPrefs.edit().putString(KEY_HYDRATION_INTAKE, jsonArray.toString()).apply()
    }
    
    fun getHydrationIntake(): List<HydrationIntake> {
        val json = sharedPrefs.getString(KEY_HYDRATION_INTAKE, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            val intakeList = mutableListOf<HydrationIntake>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val intake = HydrationIntake(
                    id = jsonObject.getString("id"),
                    amountMl = jsonObject.getInt("amountMl"),
                    date = jsonObject.getString("date"),
                    timestamp = Date(jsonObject.getLong("timestamp"))
                )
                intakeList.add(intake)
            }
            intakeList
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun addHydrationIntake(intake: HydrationIntake) {
        val intakes = getHydrationIntake().toMutableList()
        intakes.add(0, intake) // Add to beginning for chronological order
        saveHydrationIntake(intakes)
    }
    
    fun getTodayHydrationIntake(): List<HydrationIntake> {
        val today = dateFormat.format(Date())
        return getHydrationIntake().filter { it.date == today }
    }
    
    fun getTodayTotalHydration(): Int {
        return getTodayHydrationIntake().sumOf { it.amountMl }
    }

    // Reset today's hydration entries
    fun resetTodayHydration() {
        val today = dateFormat.format(Date())
        val remaining = getHydrationIntake().filter { it.date != today }
        saveHydrationIntake(remaining)
    }
    
    // APP SETTINGS
    
    fun setFirstLaunch(isFirst: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, isFirst).apply()
    }
    
    fun isFirstLaunch(): Boolean {
        return sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    fun setLastBackupTime(timestamp: Long) {
        sharedPrefs.edit().putLong(KEY_LAST_BACKUP, timestamp).apply()
    }
    
    fun getLastBackupTime(): Long {
        return sharedPrefs.getLong(KEY_LAST_BACKUP, 0)
    }
    
    // User Profile Methods
    fun setUserName(name: String) {
        sharedPrefs.edit().putString(KEY_USER_NAME, name).apply()
    }
    
    fun getUserName(): String? {
        return sharedPrefs.getString(KEY_USER_NAME, null)
    }
    
    fun setUserProfilePicPath(path: String) {
        sharedPrefs.edit().putString(KEY_USER_PROFILE_PIC, path).apply()
    }
    
    fun getUserProfilePicPath(): String? {
        return sharedPrefs.getString(KEY_USER_PROFILE_PIC, null)
    }
    
    // User Metrics Methods
    fun saveUserMetrics(metrics: UserMetrics) {
        val jsonObject = JSONObject().apply {
            put("calories", metrics.calories)
            put("heartRate", metrics.heartRate)
            put("weight", metrics.weight)
            put("steps", metrics.steps)
            put("lastUpdated", metrics.lastUpdated.time)
        }
        sharedPrefs.edit().putString(KEY_USER_METRICS, jsonObject.toString()).apply()
    }
    
    fun getUserMetrics(): UserMetrics {
        val json = sharedPrefs.getString(KEY_USER_METRICS, null)
        return if (json != null) {
            try {
                val jsonObject = JSONObject(json)
                UserMetrics(
                    calories = jsonObject.getDouble("calories").toFloat(),
                    heartRate = jsonObject.getInt("heartRate"),
                    weight = jsonObject.getDouble("weight").toFloat(),
                    steps = jsonObject.getInt("steps"),
                    lastUpdated = Date(jsonObject.getLong("lastUpdated"))
                )
            } catch (e: Exception) {
                UserMetrics()
            }
        } else {
            UserMetrics()
        }
    }
    
    // User login methods
    fun setUserLoggedIn(isLoggedIn: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    fun isUserLoggedIn(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    // UTILITY METHODS
    
    fun clearAllData() {
        sharedPrefs.edit().clear().apply()
    }
    
    fun exportData(): String {
        val root = JSONObject()

        // Habits
        val habitsArray = JSONArray()
        getHabits().forEach { habit ->
            val h = JSONObject().apply {
                put("id", habit.id)
                put("name", habit.name)
                put("description", habit.description)
                put("targetValue", habit.targetValue)
                put("unit", habit.unit)
                put("category", habit.category)
                put("createdDate", habit.createdDate.time)
                put("isActive", habit.isActive)
            }
            habitsArray.put(h)
        }
        root.put("habits", habitsArray)

        // Habit progress
        val progressArray = JSONArray()
        getHabitProgress().forEach { progress ->
            val p = JSONObject().apply {
                put("habitId", progress.habitId)
                put("date", progress.date)
                put("currentValue", progress.currentValue)
                put("isCompleted", progress.isCompleted)
                put("completionTime", progress.completionTime?.time ?: 0)
            }
            progressArray.put(p)
        }
        root.put("habitProgress", progressArray)

        // Mood entries
        val moodArray = JSONArray()
        getMoodEntries().forEach { entry ->
            val m = JSONObject().apply {
                put("id", entry.id)
                put("mood", entry.mood.name)
                put("emoji", entry.emoji)
                put("notes", entry.notes)
                put("timestamp", entry.timestamp.time)
                put("date", entry.date)
            }
            moodArray.put(m)
        }
        root.put("moodEntries", moodArray)

        // Hydration settings
        getHydrationSettings().let { settings ->
            val s = JSONObject().apply {
                put("dailyGoalMl", settings.dailyGoalMl)
                put("reminderEnabled", settings.reminderEnabled)
                put("reminderIntervalMinutes", settings.reminderIntervalMinutes)
                put("startTime", settings.startTime)
                put("endTime", settings.endTime)
                put("startMinute", settings.startMinute)
            }
            root.put("hydrationSettings", s)
        }

        // Hydration intake
        val intakeArray = JSONArray()
        getHydrationIntake().forEach { intake ->
            val i = JSONObject().apply {
                put("id", intake.id)
                put("amountMl", intake.amountMl)
                put("date", intake.date)
                put("timestamp", intake.timestamp.time)
            }
            intakeArray.put(i)
        }
        root.put("hydrationIntake", intakeArray)

        root.put("exportDate", Date().time)

        return root.toString()
    }
    
    // Get current date string
    fun getCurrentDateString(): String {
        return dateFormat.format(Date())
    }
    
    // Calculate habit streak
    fun calculateHabitStreak(habitId: String): Int {
        val progress = getHabitProgress()
            .filter { it.habitId == habitId && it.isCompleted }
            .sortedByDescending { it.date }
        
        if (progress.isEmpty()) return 0
        
        var streak = 0
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Start from today and go backwards
        for (i in 0 until 365) { // Check up to a year back
            val dateString = dateFormat.format(calendar.time)
            val hasProgress = progress.any { it.date == dateString }
            
            if (hasProgress) {
                streak++
            } else {
                break
            }
            
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return streak
    }
}