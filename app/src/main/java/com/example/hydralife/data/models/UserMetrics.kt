package com.example.hydralife.data.models

import java.util.Date

data class UserMetrics(
    val calories: Float = 0f,
    val heartRate: Int = 0,
    val weight: Float = 0f,
    val steps: Int = 0,
    val lastUpdated: Date = Date()
)