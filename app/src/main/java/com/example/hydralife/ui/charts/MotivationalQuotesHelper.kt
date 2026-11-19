package com.example.hydralife.ui.charts

import kotlin.random.Random

/**
 * Helper class for managing motivational quotes and tips
 */
class MotivationalQuotesHelper {
    
    companion object {
        private val MOTIVATIONAL_QUOTES = arrayOf(
            "Small consistent efforts lead to big results.",
            "Every day you choose to improve is a victory.",
            "Your future self will thank you for the habits you build today.",
            "Consistency is the key to success.",
            "Progress, not perfection, is what matters.",
            "The best time to start was yesterday. The next best time is now.",
            "Habits are the compound interest of self-improvement.",
            "You don't rise to the level of your goals, you fall to the level of your systems.",
            "Discipline is choosing between what you want now and what you want most.",
            "The difference between who you are and who you want to be is what you do.",
            "Success is the sum of small efforts repeated day in and day out.",
            "Don't wait for motivation. Build discipline.",
            "The only bad workout is the one that didn't happen.",
            "Your body can do it—it's your mind you need to convince.",
            "Fitness is not about being better than someone else. It's about being better than you used to be.",
            "The miracle isn't that I finished. The miracle is that I had the courage to start.",
            "Take care of your body. It's the only place you have to live.",
            "The only impossible journey is the one you never begin.",
            "The best project you can work on is you.",
            "Invest in your health today for a better tomorrow."
        )
        
        private val HEALTH_TIPS = arrayOf(
            "Drink at least 8 glasses of water daily.",
            "Get 7-9 hours of quality sleep each night.",
            "Eat a balanced diet with plenty of fruits and vegetables.",
            "Take short breaks during work to stretch and move.",
            "Practice deep breathing exercises to reduce stress.",
            "Limit processed foods and added sugars.",
            "Include strength training in your exercise routine twice a week.",
            "Spend time outdoors daily for fresh air and sunlight.",
            "Practice good posture to prevent back and neck pain.",
            "Limit screen time before bed to improve sleep quality."
        )
        
        private val PRODUCTIVITY_TIPS = arrayOf(
            "Start your day with the most important task.",
            "Use the Pomodoro Technique to maintain focus.",
            "Minimize distractions by turning off notifications.",
            "Plan your day the night before.",
            "Take regular breaks to maintain productivity.",
            "Prioritize tasks using the Eisenhower Matrix.",
            "Set specific, measurable goals for each day.",
            "Declutter your workspace for better focus.",
            "Batch similar tasks together to save time.",
            "Review your progress at the end of each day."
        )
    }
    
    /**
     * Get a random motivational quote
     *
     * @return A random motivational quote
     */
    fun getRandomMotivationalQuote(): String {
        return MOTIVATIONAL_QUOTES.random()
    }
    
    /**
     * Get a random health tip
     *
     * @return A random health tip
     */
    fun getRandomHealthTip(): String {
        return HEALTH_TIPS.random()
    }
    
    /**
     * Get a random productivity tip
     *
     * @return A random productivity tip
     */
    fun getRandomProductivityTip(): String {
        return PRODUCTIVITY_TIPS.random()
    }
    
    /**
     * Get a random tip based on category
     *
     * @param category The category of tip to get
     * @return A random tip from the specified category
     */
    fun getRandomTipByCategory(category: String): String {
        return when (category.lowercase()) {
            "health" -> getRandomHealthTip()
            "productivity" -> getRandomProductivityTip()
            else -> getRandomMotivationalQuote()
        }
    }
    
    /**
     * Get a completely random tip or quote
     *
     * @return A random tip or quote from all categories
     */
    fun getRandomTipOrQuote(): String {
        val allTips = MOTIVATIONAL_QUOTES + HEALTH_TIPS + PRODUCTIVITY_TIPS
        return allTips.random()
    }
}