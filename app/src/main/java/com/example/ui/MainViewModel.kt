package com.example.ui

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BlockedAppEntity
import com.example.data.DeepGuardDatabase
import com.example.data.DeepGuardRepository
import com.example.data.GuardSettingsEntity
import com.example.service.DeepGuardAccessibilityService
import com.example.service.DeepGuardDeviceAdminReceiver
import com.example.service.DeepGuardForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppFilter { ALL, PRIORITY_SOCIAL_GAME, OTHER }

data class PermissionState(
    val isAccessibilityGranted: Boolean = false,
    val isUsageAccessGranted: Boolean = false,
    val isDeviceAdminGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isNotificationGranted: Boolean = false
) {
    val areAllGranted: Boolean
        get() = isAccessibilityGranted && isUsageAccessGranted && isDeviceAdminGranted && isOverlayGranted && isNotificationGranted
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeepGuardRepository
    val guardSettings: StateFlow<GuardSettingsEntity>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(AppFilter.ALL)
    val currentFilter: StateFlow<AppFilter> = _currentFilter.asStateFlow()

    val filteredApps: StateFlow<List<BlockedAppEntity>>

    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    init {
        val dao = DeepGuardDatabase.getInstance(application).appDao()
        repository = DeepGuardRepository(application, dao)

        viewModelScope.launch {
            repository.syncDeviceApps()
            checkPermissions()
            startForegroundService()
        }

        guardSettings = repository.guardSettings
            .combine(MutableStateFlow(Unit)) { settings, _ ->
                settings ?: GuardSettingsEntity()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GuardSettingsEntity()
            )

        filteredApps = combine(repository.allApps, _searchQuery, _currentFilter) { apps, query, filter ->
            apps.filter { app ->
                val matchesQuery = app.appName.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
                val matchesFilter = when (filter) {
                    AppFilter.ALL -> true
                    AppFilter.PRIORITY_SOCIAL_GAME -> app.isPrioritySocialOrGame
                    AppFilter.OTHER -> !app.isPrioritySocialOrGame
                }
                matchesQuery && matchesFilter
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: AppFilter) {
        _currentFilter.value = filter
    }

    fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.updateAppBlockedStatus(packageName, isBlocked)
        }
    }

    fun setAllAppsBlocked(isBlocked: Boolean) {
        viewModelScope.launch {
            repository.setAllAppsBlocked(isBlocked)
        }
    }

    fun startTimer(durationSeconds: Long) {
        viewModelScope.launch {
            repository.startTimer(durationSeconds)
            startForegroundService()
        }
    }

    fun startTimer(durationMinutes: Int) {
        startTimer(durationMinutes * 60L)
    }

    fun stopTimer() {
        viewModelScope.launch {
            repository.stopTimer()
        }
    }

    fun checkPermissions() {
        val context = getApplication<Application>()

        val isAccessibility = isAccessibilityServiceEnabled(context, DeepGuardAccessibilityService::class.java)
        val isUsage = isUsageStatsPermissionGranted(context)
        val isDeviceAdmin = isDeviceAdminActive(context)
        val isOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
        val isNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true

        _permissionState.value = PermissionState(
            isAccessibilityGranted = isAccessibility,
            isUsageAccessGranted = isUsage,
            isDeviceAdminGranted = isDeviceAdmin,
            isOverlayGranted = isOverlay,
            isNotificationGranted = isNotif
        )
    }

    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, serviceClass)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedComponentName.flattenToString())
    }

    private fun isUsageStatsPermissionGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun isDeviceAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        val adminComponent = ComponentName(context, DeepGuardDeviceAdminReceiver::class.java)
        return dpm.isAdminActive(adminComponent)
    }

    private fun startForegroundService() {
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, DeepGuardForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
