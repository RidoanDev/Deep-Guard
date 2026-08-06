package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String = "App", // "Social", "Game", "Browser", "Other"
    val isPrioritySocialOrGame: Boolean = false,
    val isBlocked: Boolean = true
)
