package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guard_settings")
data class GuardSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isTimerActive: Boolean = false,
    val timerEndTimeMs: Long = 0L,
    val initialTimerDurationMinutes: Int = 30,
    val initialTimerDurationSeconds: Long = 1800L,
    val isAdultPornBlocked: Boolean = true,
    val isGamblingBlocked: Boolean = true,
    val isDatingBlocked: Boolean = true,
    val isBrowserUrlFilterActive: Boolean = true,
    val isVpnBypassBlocked: Boolean = true,
    val blockedCount: Int = 0
)
