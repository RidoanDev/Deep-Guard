package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.DeepGuardForegroundService
import com.example.ui.MainViewModel
import com.example.ui.screens.AppListScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.PermissionScreen
import com.example.ui.theme.DarkGreenBackground
import com.example.ui.theme.DarkGreenCardBorder
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.DeepGuardTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start background security service
        val serviceIntent = Intent(this, DeepGuardForegroundService::class.java)
        try {
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            DeepGuardTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var isFocusWebViewActive by remember { mutableStateOf(false) }

    val guardSettings by viewModel.guardSettings.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()

    val blockedAppsList = remember(filteredApps) {
        filteredApps.filter { it.isBlocked }
    }

    val showBottomBar = !(selectedTab == 2 && isFocusWebViewActive)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkGreenBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkGreenSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    val tabs = listOf(
                        Triple("Guard", Icons.Default.Shield, 0),
                        Triple("Apps", Icons.Default.Android, 1),
                        Triple("Focus", Icons.Default.GraphicEq, 2),
                        Triple("Perm", Icons.Default.Settings, 3)
                    )

                    tabs.forEach { (label, icon, index) ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedTab = index
                                if (index != 2) {
                                    isFocusWebViewActive = false
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) EmeraldLight else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldLight else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = EmeraldContainer,
                                selectedIconColor = EmeraldLight,
                                selectedTextColor = EmeraldLight,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("tab_item_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBottomBar) innerPadding else androidx.compose.foundation.layout.PaddingValues(0.dp))
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    guardSettings = guardSettings,
                    blockedAppsList = blockedAppsList,
                    areAllPermissionsGranted = permissionState.areAllGranted,
                    onStartTimer = { durationSeconds -> viewModel.startTimer(durationSeconds) },
                    onStopTimer = { viewModel.stopTimer() },
                    onNavigateToAppList = { selectedTab = 1 },
                    onNavigateToPermissions = { selectedTab = 3 }
                )
                1 -> AppListScreen(
                    appList = filteredApps,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleApp = { pkg, isBlocked -> viewModel.toggleAppBlocked(pkg, isBlocked) },
                    onSelectAll = { isBlocked -> viewModel.setAllAppsBlocked(isBlocked) },
                    isTimerActive = guardSettings.isTimerActive && System.currentTimeMillis() < guardSettings.timerEndTimeMs
                )
                2 -> FocusScreen(
                    onWebViewToggle = { active -> isFocusWebViewActive = active }
                )
                3 -> PermissionScreen(
                    permissionState = permissionState,
                    onRefreshPermissions = { viewModel.checkPermissions() }
                )
            }
        }
    }
}

