package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BlockedAppEntity
import com.example.ui.theme.DarkGreenBackground
import com.example.ui.theme.DarkGreenCardBorder
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.DarkGreenSurfaceSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun rememberAppIcon(packageName: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(packageName) {
        try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth.coerceAtLeast(48)
                val height = drawable.intrinsicHeight.coerceAtLeast(48)
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

private fun isSettingsApp(packageName: String): Boolean {
    return packageName == "com.android.settings" || packageName == "com.samsung.android.settings"
}

@Composable
fun AppListScreen(
    appList: List<BlockedAppEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    isTimerActive: Boolean = false
) {
    val filteredApps = remember(appList, searchQuery) {
        if (searchQuery.isBlank()) {
            appList
        } else {
            appList.filter { 
                it.appName.contains(searchQuery, ignoreCase = true) || 
                it.packageName.contains(searchQuery, ignoreCase = true) 
            }
        }
    }
    val priorityApps = remember(filteredApps) {
        filteredApps.filter { it.isPrioritySocialOrGame }
            .sortedWith(compareByDescending<BlockedAppEntity> { it.isBlocked }.thenBy { it.appName.lowercase() })
    }
    val otherApps = remember(filteredApps) {
        filteredApps.filter { !it.isPrioritySocialOrGame }
            .sortedWith(compareByDescending<BlockedAppEntity> { it.isBlocked }.thenBy { it.appName.lowercase() })
    }
    val selectedCount = appList.count { it.isBlocked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // TOP HEADER BAR
        Text(
            text = "App List",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // MINIMAL SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search apps...",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = DarkGreenCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkGreenSurface,
                unfocusedContainerColor = DarkGreenSurface,
                cursorColor = EmeraldPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Selected count indicator
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedCount Selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            // SECTION 1: DISTRACTION APPS
            if (priorityApps.isNotEmpty()) {
                items(priorityApps, key = { "priority_${it.packageName}" }) { app ->
                    AppItemRow(
                        app = app,
                        isTimerActive = isTimerActive,
                        onToggle = { isChecked -> onToggleApp(app.packageName, isChecked) }
                    )
                }
            }

            // SECTION 2: OTHER INSTALLED APPS
            if (otherApps.isNotEmpty()) {
                items(otherApps, key = { "other_${it.packageName}" }) { app ->
                    AppItemRow(
                        app = app,
                        isTimerActive = isTimerActive,
                        onToggle = { isChecked -> onToggleApp(app.packageName, isChecked) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItemRow(
    app: BlockedAppEntity,
    isTimerActive: Boolean = false,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val appIcon = rememberAppIcon(app.packageName)
    val isSettings = isSettingsApp(app.packageName)
    val isLockedSelected = (app.isBlocked && isSettings) || (app.isBlocked && isTimerActive)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGreenSurface)
            .clickable {
                if (app.isBlocked) {
                    if (isSettings) {
                        Toast.makeText(
                            context,
                            "সেটিংস অ্যাপটি বাধ্যতামূলকভাবে সিলেক্ট করা থাকবে, এটি বন্ধ করা যাবে না!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@clickable
                    }
                    if (isTimerActive) {
                        Toast.makeText(
                            context,
                            "টাইমার চলাকালীন সিলেক্টেড অ্যাপ আনসিলেক্ট করা যাবে না!",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@clickable
                    }
                }
                onToggle(!app.isBlocked)
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkGreenSurfaceSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (app.isPrioritySocialOrGame) Icons.Default.Games else Icons.Default.Android,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = app.appName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Custom Green Rounded Checkbox with Lock indicator when unselecting is disabled
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (app.isBlocked) EmeraldPrimary else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (app.isBlocked) EmeraldPrimary else DarkGreenCardBorder,
                    shape = RoundedCornerShape(6.dp)
                )
                .testTag("app_checkbox_${app.packageName}"),
            contentAlignment = Alignment.Center
        ) {
            if (app.isBlocked) {
                if (isLockedSelected) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked Selected",
                        tint = DarkGreenBackground,
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = DarkGreenBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


