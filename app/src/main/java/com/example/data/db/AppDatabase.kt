package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.AlertHistoryEntity
import com.example.data.model.FerryRouteEntity
import com.example.data.model.PreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FerryDao {
    @Query("SELECT * FROM ferry_routes ORDER BY id ASC")
    fun getAllRoutesFlow(): Flow<List<FerryRouteEntity>>

    @Query("SELECT * FROM ferry_routes ORDER BY id ASC")
    suspend fun getAllRoutesDirect(): List<FerryRouteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: FerryRouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<FerryRouteEntity>)

    @Update
    suspend fun updateRoute(route: FerryRouteEntity)

    @Query("UPDATE ferry_routes SET isAlertSet = :isSet WHERE id = :routeId")
    suspend fun setRouteAlert(routeId: Int, isSet: Boolean)

    @Query("UPDATE ferry_routes SET nextDepartureSeconds = :dep, nextArrivalSeconds = :arr WHERE id = :routeId")
    suspend fun updateRouteCountdowns(routeId: Int, dep: Int, arr: Int)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllAlertsFlow(): Flow<List<AlertHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertHistoryEntity)

    @Query("DELETE FROM alert_history WHERE id = :alertId")
    suspend fun deleteAlert(alertId: Int)

    @Query("DELETE FROM alert_history")
    suspend fun clearAllAlerts()
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferencesFlow(): Flow<PreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesSync(): PreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(pref: PreferenceEntity)
}

@Database(
    entities = [FerryRouteEntity::class, AlertHistoryEntity::class, PreferenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ferryDao(): FerryDao
    abstract fun alertDao(): AlertDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ferry_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
