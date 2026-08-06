package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BlockedAppEntity
import com.example.data.GuardSettingsEntity
import com.example.ui.theme.DarkGreenBackground
import com.example.ui.theme.DarkGreenCardBorder
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.DarkGreenSurfaceSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PolishBlue
import com.example.ui.theme.PolishBlueContainer
import com.example.ui.theme.PolishCardBorder
import com.example.ui.theme.PolishHeaderDark
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceSecondary
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import com.example.ui.theme.WarningRedContainer
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    guardSettings: GuardSettingsEntity,
    blockedAppsList: List<BlockedAppEntity>,
    areAllPermissionsGranted: Boolean = true,
    onStartTimer: (Long) -> Unit,
    onStopTimer: () -> Unit = {},
    onNavigateToAppList: () -> Unit,
    onNavigateToPermissions: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    var showPermissionWarningDialog by remember { mutableStateOf(false) }

    // Hidden Emergency Unlock tap counter on status capsule
    var statusCapsuleTapCount by remember { mutableIntStateOf(0) }
    var lastTapTimeMs by remember { mutableLongStateOf(0L) }

    // Clock style HH:MM:SS picker states
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(15) }
    var selectedSeconds by remember { mutableIntStateOf(0) }

    // Real-time local countdown tracker
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(guardSettings.isTimerActive) {
        while (guardSettings.isTimerActive) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingMs = (guardSettings.timerEndTimeMs - currentTimeMs).coerceAtLeast(0L)
    val isTimerRunning = guardSettings.isTimerActive && remainingMs > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // TOP HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "DeepGuard Logo",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DeepGuard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // SYSTEM ACTIVE BADGE (7 taps hidden emergency unlock)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0C2B1D))
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTimeMs > 2000L) {
                            statusCapsuleTapCount = 1
                        } else {
                            statusCapsuleTapCount++
                        }
                        lastTapTimeMs = now

                        if (statusCapsuleTapCount >= 7) {
                            statusCapsuleTapCount = 0
                            onStopTimer()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HERO TIMER CARD (DIGITAL FLIP CLOCK DESIGN AS SHOWN IN SCREENSHOT)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF09150F)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isTimerRunning) {
                    val remainingSeconds = (remainingMs / 1000) % 60
                    val remainingMinutes = ((remainingMs / 1000) / 60) % 60
                    val remainingHours = (remainingMs / 1000) / 3600

                    val timerText = String.format("%02d : %02d : %02d", remainingHours, remainingMinutes, remainingSeconds)

                    Text(
                        text = "RESTRICTED TIME REMAINING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = timerText,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val totalDurationMs = (guardSettings.initialTimerDurationSeconds.coerceAtLeast(1L) * 1000L)
                    val progress = (remainingMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = EmeraldPrimary,
                        trackColor = Color(0xFF142C20)
                    )
                } else {
                    // CLOCK PICKER CONTROLS (+/- BUTTONS)
                    val totalSeconds = (selectedHours * 3600L) + (selectedMinutes * 60L) + selectedSeconds

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ClockPickerColumn(
                            value = selectedHours,
                            label = "HH",
                            onIncrement = { if (selectedHours < 99) selectedHours++ },
                            onDecrement = { if (selectedHours > 0) selectedHours-- }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        ClockPickerColumn(
                            value = selectedMinutes,
                            label = "MM",
                            onIncrement = {
                                if (selectedMinutes < 59) selectedMinutes++
                                else { selectedMinutes = 0; if (selectedHours < 99) selectedHours++ }
                            },
                            onDecrement = {
                                if (selectedMinutes > 0) selectedMinutes--
                                else if (selectedHours > 0) { selectedHours--; selectedMinutes = 59 }
                            }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        ClockPickerColumn(
                            value = selectedSeconds,
                            label = "SS",
                            onIncrement = {
                                if (selectedSeconds < 59) selectedSeconds++
                                else { selectedSeconds = 0; if (selectedMinutes < 59) selectedMinutes++ }
                            },
                            onDecrement = {
                                if (selectedSeconds > 0) selectedSeconds--
                                else if (selectedMinutes > 0) { selectedMinutes--; selectedSeconds = 59 }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val formattedDurationText = when {
                        totalSeconds < 60 -> "${totalSeconds}s"
                        totalSeconds % 3600 == 0L -> "${totalSeconds / 3600}h"
                        totalSeconds % 60 == 0L -> "${totalSeconds / 60}m"
                        else -> "${totalSeconds / 60}m ${totalSeconds % 60}s"
                    }

                    Button(
                        onClick = {
                            if (totalSeconds >= 1) {
                                if (!areAllPermissionsGranted) {
                                    showPermissionWarningDialog = true
                                } else {
                                    onStartTimer(totalSeconds)
                                }
                            }
                        },
                        enabled = totalSeconds >= 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_timer_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = DarkGreenBackground,
                            disabledContainerColor = Color(0xFF142C20),
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = DarkGreenBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (totalSeconds >= 1) "START $formattedDurationText FOCUS LOCK" else "SELECT DURATION",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (showPermissionWarningDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showPermissionWarningDialog = false },
                containerColor = DarkGreenSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "পারমিশন প্রয়োজন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Text(
                        text = "টাইমার অন করার জন্য সকল পারমিশন (Accessibility, Usage Access, Device Admin, Overlay, Notification) দেওয়া আবশ্যক।\n\nঅনুগ্রহ করে সকল পারমিশন Grant করুন।",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionWarningDialog = false
                            onNavigateToPermissions()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = DarkGreenBackground
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "পারমিশন পেইজে যান",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showPermissionWarningDialog = false }
                    ) {
                        Text(
                            text = "বাতিল",
                            color = TextSecondary
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SELECTED APPS CARD (MATCHING SCREENSHOT)
        val blockedAppsOnlyCount = remember(blockedAppsList) {
            blockedAppsList.count { it.isBlocked }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Selected Apps",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$blockedAppsOnlyCount",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0C2B1D))
                            .border(1.dp, EmeraldPrimary, RoundedCornerShape(20.dp))
                            .clickable { onNavigateToAppList() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("manage_apps_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Manage Apps",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuardMiniStatusItem(
    label: String,
    status: String,
    isBlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PolishSurfaceSecondary)
            .border(1.dp, PolishCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ShieldGreen
            )
        }
    }
}

@Composable
fun ClockPickerColumn(
    value: Int,
    label: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C2B1D))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase $label", tint = EmeraldPrimary)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF09150F))
                .border(1.dp, DarkGreenCardBorder, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount
                        if (accumulatedDrag <= -20f) {
                            onIncrement()
                            accumulatedDrag = 0f
                        } else if (accumulatedDrag >= 20f) {
                            onDecrement()
                            accumulatedDrag = 0f
                        }
                    }
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C2B1D))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease $label", tint = EmeraldPrimary)
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun DashboardAppIconItem(app: BlockedAppEntity) {
    val iconBitmap = rememberAppIcon(app.packageName)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PolishSurfaceSecondary)
                .border(1.dp, PolishCardBorder, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.appName,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                Icon(
                    imageVector = if (app.isPrioritySocialOrGame) Icons.Default.Games else Icons.Default.Android,
                    contentDescription = app.appName,
                    tint = PolishBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = app.appName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

