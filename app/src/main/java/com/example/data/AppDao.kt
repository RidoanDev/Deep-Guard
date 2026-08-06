package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM blocked_apps ORDER BY isPrioritySocialOrGame DESC, appName ASC")
    fun getAllApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getBlockedAppsListDirect(): List<BlockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<BlockedAppEntity>)

    @Query("DELETE FROM blocked_apps")
    suspend fun deleteAllApps()

    @Query("UPDATE blocked_apps SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun updateAppBlockedStatus(packageName: String, isBlocked: Boolean)

    @Query("UPDATE blocked_apps SET isBlocked = :isBlocked")
    suspend fun setAllAppsBlocked(isBlocked: Boolean)

    @Query("SELECT * FROM guard_settings WHERE id = 1")
    fun getGuardSettings(): Flow<GuardSettingsEntity?>

    @Query("SELECT * FROM guard_settings WHERE id = 1")
    suspend fun getGuardSettingsDirect(): GuardSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGuardSettings(settings: GuardSettingsEntity)

    @Query("UPDATE guard_settings SET isTimerActive = :isActive, timerEndTimeMs = :endTimeMs WHERE id = 1")
    suspend fun updateTimerState(isActive: Boolean, endTimeMs: Long)

    @Query("UPDATE guard_settings SET blockedCount = blockedCount + 1 WHERE id = 1")
    suspend fun incrementBlockedCount()
}
