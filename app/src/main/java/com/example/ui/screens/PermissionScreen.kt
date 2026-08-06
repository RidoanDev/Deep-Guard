package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.DeepGuardDeviceAdminReceiver
import com.example.ui.PermissionState
import com.example.ui.theme.DarkGreenBackground
import com.example.ui.theme.DarkGreenCardBorder
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PermissionScreen(
    permissionState: PermissionState,
    onRefreshPermissions: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val allGranted = permissionState.isAccessibilityGranted &&
            permissionState.isDeviceAdminGranted &&
            permissionState.isOverlayGranted &&
            permissionState.isUsageAccessGranted &&
            permissionState.isNotificationGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER
        Text(
            text = "Permissions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))



        // 1. ACCESSIBILITY SERVICE
        PermissionCardRow(
            title = "Accessibility Service",
            subtitle = if (permissionState.isAccessibilityGranted) "Active" else "Tap to grant",
            isGranted = permissionState.isAccessibilityGranted,
            onGrant = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            tag = "grant_accessibility_btn"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. DEVICE ADMIN (UNINSTALL DEFENSE)
        PermissionCardRow(
            title = "Device Admin",
            subtitle = if (permissionState.isDeviceAdminGranted) "Active" else "Tap to grant",
            isGranted = permissionState.isDeviceAdminGranted,
            onGrant = {
                val comp = ComponentName(context, DeepGuardDeviceAdminReceiver::class.java)
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Activate device administrator to prevent DeepGuard uninstallation."
                    )
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e2: Exception) {
                        try {
                            val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(fallbackIntent)
                        } catch (e3: Exception) {
                            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(settingsIntent)
                        }
                    }
                }
            },
            tag = "grant_device_admin_btn"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. DISPLAY OVER OTHER APPS (SYSTEM OVERLAY)
        PermissionCardRow(
            title = "Display Over Other Apps",
            subtitle = if (permissionState.isOverlayGranted) "Active" else "Tap to grant",
            isGranted = permissionState.isOverlayGranted,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    context.startActivity(intent)
                }
            },
            tag = "grant_overlay_btn"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. USAGE ACCESS
        PermissionCardRow(
            title = "Usage Access",
            subtitle = if (permissionState.isUsageAccessGranted) "Active" else "Tap to grant",
            isGranted = permissionState.isUsageAccessGranted,
            onGrant = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            tag = "grant_usage_btn"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 5. NOTIFICATION PERMISSION
        PermissionCardRow(
            title = "Notification Permission",
            subtitle = if (permissionState.isNotificationGranted) "Active" else "Tap to grant",
            isGranted = permissionState.isNotificationGranted,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            },
            tag = "grant_notif_btn"
        )
    }
}

@Composable
fun PermissionCardRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGrant() }
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C2B1D)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = if (isGranted) EmeraldPrimary else Color(0xFFEF4444)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isGranted) EmeraldPrimary else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (isGranted) EmeraldPrimary else Color(0xFFEF4444),
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = DarkGreenBackground,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Required",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}


