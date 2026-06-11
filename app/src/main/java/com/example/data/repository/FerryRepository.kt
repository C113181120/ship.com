package com.example.data.repository

import com.example.data.db.FerryDao
import com.example.data.db.AlertDao
import com.example.data.db.PreferenceDao
import com.example.data.model.FerryRouteEntity
import com.example.data.model.AlertHistoryEntity
import com.example.data.model.PreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FerryRepository(
    private val ferryDao: FerryDao,
    private val alertDao: AlertDao,
    private val preferenceDao: PreferenceDao
) {
    val allRoutes: Flow<List<FerryRouteEntity>> = ferryDao.getAllRoutesFlow()
    val allAlerts: Flow<List<AlertHistoryEntity>> = alertDao.getAllAlertsFlow()
    val preferences: Flow<PreferenceEntity?> = preferenceDao.getPreferencesFlow()

    suspend fun initializeDatabase() {
        // Initialize preferences if empty
        val existingPref = preferenceDao.getPreferencesSync()
        if (existingPref == null) {
            preferenceDao.insertOrUpdatePreferences(PreferenceEntity())
        }

        // Initialize routes if empty
        val existingRoutes = ferryDao.getAllRoutesDirect()
        if (existingRoutes.isEmpty()) {
            val defaultRoutes = listOf(
                FerryRouteEntity(
                    routeNameZh = "鼓山 ⇄ 旗津 (高雄)",
                    routeNameEn = "Gushan ⇄ Cijin (Kaohsiung)",
                    departingPortZh = "鼓山渡輪站",
                    departingPortEn = "Gushan Port",
                    arrivingPortZh = "旗津輪渡站",
                    arrivingPortEn = "Cijin Port",
                    baseIntervalMinutes = 10,
                    currentIntervalMinutes = 10,
                    nextDepartureSeconds = 340, // 5m 40s
                    nextArrivalSeconds = 720,    // 12m
                    passengerFlowPct = 55,
                    isAlertSet = false
                ),
                FerryRouteEntity(
                    routeNameZh = "淡水 ⇄ 八里 (新北)",
                    routeNameEn = "Tamsui ⇄ Bali (New Taipei)",
                    departingPortZh = "淡水老街碼頭",
                    departingPortEn = "Tamsui Pier",
                    arrivingPortZh = "八里渡船頭碼頭",
                    arrivingPortEn = "Bali Pier",
                    baseIntervalMinutes = 15,
                    currentIntervalMinutes = 15,
                    nextDepartureSeconds = 620, // 10m 20s
                    nextArrivalSeconds = 1120,   // 18m 40s
                    passengerFlowPct = 30,
                    isAlertSet = false
                ),
                FerryRouteEntity(
                    routeNameZh = "東港 ⇄ 小琉球 (屏東)",
                    routeNameEn = "Donggang ⇄ Xiaoliuqiu (Pingtung)",
                    departingPortZh = "東港碼頭",
                    departingPortEn = "Donggang Pier",
                    arrivingPortZh = "小琉球白沙尾港",
                    arrivingPortEn = "Xiaoliuqiu Port",
                    baseIntervalMinutes = 30,
                    currentIntervalMinutes = 30,
                    nextDepartureSeconds = 1200, // 20m
                    nextArrivalSeconds = 2400,   // 40m
                    passengerFlowPct = 78,
                    isAlertSet = false
                ),
                FerryRouteEntity(
                    routeNameZh = "基隆 ⇄ 馬祖 (基馬客輪)",
                    routeNameEn = "Keelung ⇄ Matsu (Offshore Rail)",
                    departingPortZh = "基隆港西二碼頭",
                    departingPortEn = "Keelung Port",
                    arrivingPortZh = "馬祖福澳港",
                    arrivingPortEn = "Matsu Fuao Port",
                    baseIntervalMinutes = 240,
                    currentIntervalMinutes = 240,
                    nextDepartureSeconds = 9800, // 2h 43m
                    nextArrivalSeconds = 14200,  // 3h 56m
                    passengerFlowPct = 42,
                    isAlertSet = false
                )
            )
            ferryDao.insertRoutes(defaultRoutes)
        }
    }

    suspend fun updatePreferences(pref: PreferenceEntity) {
        preferenceDao.insertOrUpdatePreferences(pref)
    }

    suspend fun getPreferencesSync(): PreferenceEntity? {
        return preferenceDao.getPreferencesSync()
    }

    suspend fun updateRouteCountdowns(id: Int, depSeconds: Int, arrSeconds: Int) {
        ferryDao.updateRouteCountdowns(id, depSeconds, arrSeconds)
    }

    suspend fun updateRoute(route: FerryRouteEntity) {
        ferryDao.updateRoute(route)
    }

    suspend fun setRouteAlert(routeId: Int, isSet: Boolean) {
        ferryDao.setRouteAlert(routeId, isSet)
    }

    suspend fun insertAlert(alert: AlertHistoryEntity) {
        alertDao.insertAlert(alert)
    }

    suspend fun clearAlerts() {
        alertDao.clearAllAlerts()
    }

    suspend fun getAllRoutesDirect(): List<FerryRouteEntity> {
        return ferryDao.getAllRoutesDirect()
    }
}
