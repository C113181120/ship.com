package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ferry_routes")
data class FerryRouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routeNameZh: String,
    val routeNameEn: String,
    val departingPortZh: String,
    val departingPortEn: String,
    val arrivingPortZh: String,
    val arrivingPortEn: String,
    val baseIntervalMinutes: Int, // The theoretical normal frequency (e.g., 15 minutes)
    val currentIntervalMinutes: Int, // Adjusts based on conditions/emergencies
    val nextDepartureSeconds: Int, // Active countdown in seconds
    val nextArrivalSeconds: Int,   // Active countdown in seconds
    val passengerFlowPct: Int,    // Current seating level / occupancy (e.g. 65%)
    val isAlertSet: Boolean = false // Track if reminder is turned on
)

@Entity(tableName = "alert_history")
data class AlertHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleZh: String,
    val titleEn: String,
    val messageZh: String,
    val messageEn: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEmergency: Boolean = false
)

@Entity(tableName = "user_preferences")
data class PreferenceEntity(
    @PrimaryKey val id: Int = 1, // Single preference configuration row
    val language: String = "zh", // "zh" for Trad. Chinese, "en" for English
    val windSpeedKnots: Float = 8.5f,
    val waveHeightMeters: Float = 0.4f,
    val passengerDensity: String = "medium", // low, medium, high
    val activeEmergencyId: String = "none" // none, gust, engine, congestion, rain
)
