package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.db.AppDatabase
import com.example.data.model.AlertHistoryEntity
import com.example.data.model.FerryRouteEntity
import com.example.data.model.PreferenceEntity
import com.example.data.repository.FerryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FerryViewModel(private val repository: FerryRepository) : ViewModel() {

    // Language and environmental thresholds
    val preferences: StateFlow<PreferenceEntity> = repository.preferences
        .combine(MutableStateFlow(PreferenceEntity())) { dbPref, default ->
            dbPref ?: default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PreferenceEntity()
        )

    // Dynamic reactive ferry route list
    val routeList: StateFlow<List<FerryRouteEntity>> = repository.allRoutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Log of generated dynamic alerts & notifications (persisted via DB)
    val alertLogs: StateFlow<List<AlertHistoryEntity>> = repository.allAlerts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI-specific transient state
    private val _isLoadingAI = MutableStateFlow(false)
    val isLoadingAI: StateFlow<Boolean> = _isLoadingAI.asStateFlow()

    private val _aiReportResult = MutableStateFlow<String?>(null)
    val aiReportResult: StateFlow<String?> = _aiReportResult.asStateFlow()

    private val _selectedRouteIdForAI = MutableStateFlow<Int?>(null)
    val selectedRouteIdForAI: StateFlow<Int?> = _selectedRouteIdForAI.asStateFlow()

    private var tickingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            // Set the first route as selected for AI analysis
            val routes = repository.getAllRoutesDirect()
            if (routes.isNotEmpty()) {
                _selectedRouteIdForAI.value = routes[0].id
            }
            startTimerTicks()
        }
    }

    /**
     * Periodically ticks down countdown timers for departures and arrivals every single second.
     * Integrates with safety trigger to post a notification alert when countdown is complete.
     */
    private fun startTimerTicks() {
        tickingJob?.cancel()
        tickingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentRoutes = repository.getAllRoutesDirect()
                val prefs = preferences.value

                currentRoutes.forEach { route ->
                    var newDep = route.nextDepartureSeconds - 1
                    var newArr = route.nextArrivalSeconds - 1

                    // Reset and trigger notification alerts upon departure/arrival count
                    if (newDep <= 0) {
                        val intervalSecs = route.currentIntervalMinutes * 60
                        newDep = intervalSecs
                        
                        // Push arrival/departure notification to database if toggled on
                        if (route.isAlertSet) {
                            triggerDeparturePushNotification(route)
                        }
                    }

                    if (newArr <= 0) {
                        newArr = (route.currentIntervalMinutes * 60) + 180 // arrival lags departure slightly
                        if (route.isAlertSet) {
                            triggerArrivalPushNotification(route)
                        }
                    }

                    repository.updateRouteCountdowns(route.id, newDep, newArr)
                }
            }
        }
    }

    /**
     * Adjusts the overall schedules and predictive intervals of ALL boats based on
     * changing wind, wave heights, and emergency status.
     */
    fun onUpdateEnvironment(
        windSpeed: Float,
        waveHeight: Float,
        density: String,
        emergencyId: String = preferences.value.activeEmergencyId
    ) {
        viewModelScope.launch {
            val oldPrefs = preferences.value
            val isEmergencyChanged = oldPrefs.activeEmergencyId != emergencyId

            val updatedPref = PreferenceEntity(
                id = 1,
                language = oldPrefs.language,
                windSpeedKnots = windSpeed,
                waveHeightMeters = waveHeight,
                passengerDensity = density,
                activeEmergencyId = emergencyId
            )
            repository.updatePreferences(updatedPref)

            // Recalculate intervals and countdowns across routes
            val routes = repository.getAllRoutesDirect()
            routes.forEach { route ->
                val calculatedMinutes = calculateDynamicInterval(
                    route.id,
                    route.baseIntervalMinutes,
                    windSpeed,
                    waveHeight,
                    density,
                    emergencyId
                )

                val newRoute = route.copy(
                    currentIntervalMinutes = calculatedMinutes,
                    // If current countdown exceeds new interval, scale it down
                    nextDepartureSeconds = route.nextDepartureSeconds.coerceAtMost(calculatedMinutes * 60),
                    nextArrivalSeconds = route.nextArrivalSeconds.coerceAtMost((calculatedMinutes * 60) + 180)
                )
                repository.updateRoute(newRoute)
            }

            // Post notification if a new emergency is declared
            if (isEmergencyChanged && emergencyId != "none") {
                triggerEmergencyNotification(emergencyId)
            }
        }
    }

    /**
     * Core dynamic math model for predictive ferry routing intervals
     */
    private fun calculateDynamicInterval(
        routeId: Int,
        baseMin: Int,
        wind: Float,
        wave: Float,
        density: String,
        emergency: String
    ): Int {
        var addedMins = 0
        
        // 1. Wind speed factor
        if (wind > 12f) {
            addedMins += ((wind - 12f) * 0.5f).toInt()
        }
        
        // 2. Wave height factor (large influence for long transits like Xiaoliuqiu - Route 3)
        if (wave > 0.6f) {
            val waveMult = if (routeId == 3) 12 else 6
            addedMins += ((wave - 0.6f) * waveMult).toInt()
        }

        // 3. Passenger density boarding slowdown
        if (density == "high") {
            addedMins += if (routeId == 4) 20 else 3
        }

        // 4. Incident factor
        addedMins += when (emergency) {
            "gust" -> 8
            "engine" -> 12
            "congestion" -> 6
            "rain" -> 4
            else -> 0
        }

        return (baseMin + addedMins).coerceAtLeast(baseMin)
    }

    /**
     * Toggle individual boat route notification alarms
     */
    fun toggleRouteReminder(routeId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setRouteAlert(routeId, isEnabled)
            
            // Log setting change to alert histories
            val routes = repository.getAllRoutesDirect()
            val targeted = routes.find { it.id == routeId } ?: return@launch
            
            val msgZh = if (isEnabled) "已開啟「${targeted.routeNameZh}」的動態到站提示" else "已取消「${targeted.routeNameZh}」的到站提示"
            val msgEn = if (isEnabled) "Departure alert turned ON for ${targeted.routeNameEn}" else "Departure alert turned OFF for ${targeted.routeNameEn}"

            repository.insertAlert(
                AlertHistoryEntity(
                    titleZh = "提醒設置更新",
                    titleEn = "Alert Toggled",
                    messageZh = msgZh,
                    messageEn = msgEn,
                    isEmergency = false
                )
            )
        }
    }

    /**
     * Flip between Traditional Chinese, English, Japanese, and Korean
     */
    fun toggleLanguage() {
        viewModelScope.launch {
            val prefs = preferences.value
            val nextLang = when (prefs.language) {
                "zh" -> "en"
                "en" -> "ja"
                "ja" -> "ko"
                else -> "zh"
            }
            repository.updatePreferences(prefs.copy(language = nextLang))
            
            // clear AI report to avoid translation discrepancies
            _aiReportResult.value = null
        }
    }

    fun setSelectedRouteForAI(routeId: Int) {
        _selectedRouteIdForAI.value = routeId
    }

    /**
     * Submit variables to Gemini Client for detailed maritime shipping report
     */
    fun requestAISmartReport() {
        val targetId = _selectedRouteIdForAI.value ?: return
        val prefs = preferences.value
        
        viewModelScope.launch {
            _isLoadingAI.value = true
            val routes = repository.getAllRoutesDirect()
            val targeted = routes.find { it.id == targetId }
            
            if (targeted != null) {
                val report = GeminiClient.getFerryAIPrediction(
                    lang = prefs.language,
                    routeZh = targeted.routeNameZh,
                    routeEn = targeted.routeNameEn,
                    windKnots = prefs.windSpeedKnots,
                    waveMeters = prefs.waveHeightMeters,
                    density = prefs.passengerDensity,
                    emergencyId = prefs.activeEmergencyId
                )
                _aiReportResult.value = report
            } else {
                _aiReportResult.value = if (prefs.language == "zh") "找不到選定的路線進行分析。" else "Could not find selected route for analysis."
            }
            _isLoadingAI.value = false
        }
    }

    fun dismissAIReport() {
        _aiReportResult.value = null
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearAlerts()
        }
    }

    // --- Private Notification Triggers ---

    private suspend fun triggerDeparturePushNotification(route: FerryRouteEntity) {
        repository.insertAlert(
            AlertHistoryEntity(
                titleZh = "🚢 船班離站推播",
                titleEn = "🚢 Vessel Departure Announcement",
                messageZh = "船次【${route.routeNameZh}】已安全離港起航！下一班將於 ${route.currentIntervalMinutes} 分鐘後發船。",
                messageEn = "[${route.routeNameEn}] has departed the harbor. The next vessel is scheduled to leave in ${route.currentIntervalMinutes} minutes.",
                isEmergency = false
            )
        )
    }

    private suspend fun triggerArrivalPushNotification(route: FerryRouteEntity) {
        repository.insertAlert(
            AlertHistoryEntity(
                titleZh = "⚓ 船班即將到站",
                titleEn = "⚓ Vessel Arriving Alert",
                messageZh = "您關注的【${route.routeNameZh}】正減速駛入【${route.arrivingPortZh}】。請前往登船區候車排隊。",
                messageEn = "Vessel [${route.routeNameEn}] is decelerating into [${route.arrivingPortEn}]. Please prepare to board.",
                isEmergency = false
            )
        )
    }

    private suspend fun triggerEmergencyNotification(emergencyId: String) {
        val titleZh: String
        val titleEn: String
        val detailZh: String
        val detailEn: String

        when (emergencyId) {
            "gust" -> {
                titleZh = "🚨 突發強風警報"
                titleEn = "🚨 Heavy Gust Alert"
                detailZh = "港區觀測到強烈陣風，部分開闊海域航線發船間隔自動拉長，請隨時注意安全動態。"
                detailEn = "Strong visual gust measured at docks. Open sea schedules automatically widened for boat stability."
            }
            "engine" -> {
                titleZh = "⚙️ 船隻維修通知"
                titleEn = "⚙️ Vessel Repair Advisory"
                detailZh = "主力雙體客輪突發機件檢修，我們已啟動備載渡輪，航線將進入降頻發船調度。"
                detailEn = "Unexpected machinery cooling schedule on primary vessel. Backup ship registered, frequency lowered."
            }
            "congestion" -> {
                titleZh = "⚓ 港內航道堵塞"
                titleEn = "⚓ Port Channel Congestion"
                detailZh = "航道有其他大型載運船舶慢行穿梭，渡輪採取限速讓行，船班將延誤 5-10 分鐘。"
                detailEn = "Harbor cargo transits traversing. Waterway collision clearance active, minor delays expected across piers."
            }
            "rain" -> {
                titleZh = "🌧️ 局部對流暴雨"
                titleEn = "🌧️ Squall & Visibility Notice"
                detailZh = "突發局部驟雨导致海上能見度受阻，各船長已開啟引航雷達並減速前進。"
                detailEn = "Passing storm cells lower dock line views. Radar tracking enabled, cruising speeds slowed for security."
            }
            else -> return
        }

        repository.insertAlert(
            AlertHistoryEntity(
                titleZh = titleZh,
                titleEn = titleEn,
                messageZh = detailZh,
                messageEn = detailEn,
                isEmergency = true
            )
        )
    }
}

class FerryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FerryViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repo = FerryRepository(db.ferryDao(), db.alertDao(), db.preferenceDao())
            @Suppress("UNCHECKED_CAST")
            return FerryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
