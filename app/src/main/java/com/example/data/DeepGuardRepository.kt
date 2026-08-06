package com.example.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DeepGuardRepository(
    private val context: Context,
    private val appDao: AppDao
) {
    val allApps: Flow<List<BlockedAppEntity>> = appDao.getAllApps()
    val blockedApps: Flow<List<BlockedAppEntity>> = appDao.getBlockedApps()
    val guardSettings: Flow<GuardSettingsEntity?> = appDao.getGuardSettings()

    // Pre-defined target package names and keywords specified by user
    private val priorityAppsMap = mapOf(
        "com.facebook.katana" to "Facebook",
        "com.facebook.lite" to "Facebook Lite",
        "com.instagram.android" to "Instagram",
        "com.instagram.lite" to "Instagram Lite",
        "com.whatsapp" to "WhatsApp",
        "com.whatsapp.w4b" to "WhatsApp Business",
        "org.telegram.messenger" to "Telegram",
        "com.twitter.android" to "X (Twitter)",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.instagram.barcelona" to "Threads",
        "com.google.android.youtube" to "YouTube",
        "com.dts.freefireth" to "Free Fire",
        "com.dts.freefiremax" to "Free Fire MAX",
        "com.tencent.ig" to "PUBG Mobile",
        "com.pubg.krmobile" to "PUBG Mobile KR",
        "com.pubg.newstate" to "PUBG New State",
        "com.mobile.legends" to "Mobile Legends",
        "com.snapchat.android" to "Snapchat",
        "com.discord" to "Discord",
        "com.reddit.frontpage" to "Reddit",
        "com.pinterest" to "Pinterest"
    )

    private val socialGameKeywords = listOf(
        "facebook", "instagram", "whatsapp", "telegram", "twitter", "tiktok", "threads",
        "youtube", "freefire", "pubg", "game", "poker", "casino", "dating", "tinder",
        "badoo", "snapchat", "reddit", "discord", "viber", "imo", "likee"
    )

    suspend fun syncDeviceApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installedPackages = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList<ApplicationInfo>()
        }

        val existingAppsList = appDao.getBlockedAppsListDirect()
        val existingMap = existingAppsList.associateBy { it.packageName }

        val appEntities = mutableListOf<BlockedAppEntity>()

        // Process only applications currently installed on the device
        installedPackages.forEach { appInfo ->
            val pkg = appInfo.packageName
            // Skip DeepGuard itself and core system android package
            if (pkg == context.packageName || pkg == "android") return@forEach

            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val launchIntent = pm.getLaunchIntentForPackage(pkg)

            // Skip internal system processes that lack launch intent
            if (launchIntent == null && isSystemApp) return@forEach

            val appName = pm.getApplicationLabel(appInfo).toString()
            val lowerPkg = pkg.lowercase()
            val lowerName = appName.lowercase()
            val isSocialOrGame = priorityAppsMap.containsKey(pkg) || socialGameKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }

            val isSettings = pkg == "com.android.settings" || pkg == "com.samsung.android.settings"
            val existing = existingMap[pkg]
            val isBlockedDefault = if (isSettings) true else (existing?.isBlocked ?: isSocialOrGame)

            appEntities.add(
                BlockedAppEntity(
                    packageName = pkg,
                    appName = appName,
                    category = if (isSocialOrGame) "Social / Game" else "Application",
                    isPrioritySocialOrGame = isSocialOrGame,
                    isBlocked = isBlockedDefault
                )
            )
        }

        // Always ensure system Settings app exists and is blocked
        val hasSettings = appEntities.any { it.packageName == "com.android.settings" || it.packageName == "com.samsung.android.settings" }
        if (!hasSettings) {
            appEntities.add(
                BlockedAppEntity(
                    packageName = "com.android.settings",
                    appName = "Settings",
                    category = "System",
                    isPrioritySocialOrGame = false,
                    isBlocked = true
                )
            )
        }

        appDao.deleteAllApps()
        appDao.insertApps(appEntities)

        // Ensure default settings exist
        if (appDao.getGuardSettingsDirect() == null) {
            appDao.saveGuardSettings(GuardSettingsEntity())
        }
    }

    private fun isSettingsApp(packageName: String): Boolean {
        return packageName == "com.android.settings" || packageName == "com.samsung.android.settings"
    }

    suspend fun updateAppBlockedStatus(packageName: String, isBlocked: Boolean) {
        if (isSettingsApp(packageName) && !isBlocked) {
            return
        }
        val settings = appDao.getGuardSettingsDirect()
        val isTimerActive = settings?.isTimerActive == true && System.currentTimeMillis() < (settings?.timerEndTimeMs ?: 0L)
        if (isTimerActive && !isBlocked) {
            return
        }
        appDao.updateAppBlockedStatus(packageName, isBlocked)
    }

    suspend fun setAllAppsBlocked(isBlocked: Boolean) {
        val settings = appDao.getGuardSettingsDirect()
        val isTimerActive = settings?.isTimerActive == true && System.currentTimeMillis() < (settings?.timerEndTimeMs ?: 0L)
        if (isTimerActive && !isBlocked) {
            return
        }
        appDao.setAllAppsBlocked(isBlocked)
        // Ensure Settings app remains blocked
        appDao.updateAppBlockedStatus("com.android.settings", true)
        appDao.updateAppBlockedStatus("com.samsung.android.settings", true)
    }

    suspend fun startTimer(durationSeconds: Long) {
        val endTimeMs = System.currentTimeMillis() + (durationSeconds * 1000L)
        val durationMins = (durationSeconds / 60).toInt().coerceAtLeast(1)
        val currentSettings = appDao.getGuardSettingsDirect() ?: GuardSettingsEntity()
        appDao.saveGuardSettings(
            currentSettings.copy(
                isTimerActive = true,
                timerEndTimeMs = endTimeMs,
                initialTimerDurationMinutes = durationMins,
                initialTimerDurationSeconds = durationSeconds
            )
        )
    }

    suspend fun checkAndUpdateTimer() {
        val settings = appDao.getGuardSettingsDirect() ?: return
        if (settings.isTimerActive) {
            if (System.currentTimeMillis() >= settings.timerEndTimeMs) {
                appDao.updateTimerState(isActive = false, endTimeMs = 0L)
            }
        }
    }

    suspend fun stopTimer() {
        appDao.updateTimerState(isActive = false, endTimeMs = 0L)
    }

    suspend fun incrementBlockedCount() {
        appDao.incrementBlockedCount()
    }
}
