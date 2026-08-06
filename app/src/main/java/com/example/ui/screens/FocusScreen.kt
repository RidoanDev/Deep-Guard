package com.example.ui.screens

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DarkGreenBackground
import com.example.ui.theme.DarkGreenCardBorder
import com.example.ui.theme.DarkGreenSurface
import com.example.ui.theme.DarkGreenSurfaceSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class AmbientSoundTile(
    val title: String,
    val subtitle: String,
    val url: String,
    val icon: ImageVector
)

data class MusicItem(
    val title: String,
    val url: String
)

data class FocusCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val doList: List<String>,
    val musicList: List<MusicItem>
)

@Composable
fun FocusScreen(
    onWebViewToggle: (Boolean) -> Unit = {}
) {
    var activeWebUrl by remember { mutableStateOf<String?>(null) }
    var activeWebTitle by remember { mutableStateOf("") }

    LaunchedEffect(activeWebUrl) {
        onWebViewToggle(activeWebUrl != null)
    }

    if (activeWebUrl != null) {
        InAppWebViewScreen(
            url = activeWebUrl!!,
            title = activeWebTitle,
            onClose = {
                activeWebUrl = null
                activeWebTitle = ""
            }
        )
    } else {
        FocusMainContent(
            onOpenWeb = { url, title ->
                activeWebUrl = url
                activeWebTitle = title
            }
        )
    }
}

@Composable
fun FocusMainContent(
    onOpenWeb: (url: String, title: String) -> Unit
) {
    val ambientSounds = remember {
        listOf(
            AmbientSoundTile(
                title = "Rainy Mood",
                subtitle = "Listen on RainyMood.com",
                url = "https://rainymood.com",
                icon = Icons.Default.WaterDrop
            ),
            AmbientSoundTile(
                title = "A Soft Murmur",
                subtitle = "Open ASoftMurmur.com",
                url = "https://asoftmurmur.com",
                icon = Icons.Default.Headphones
            ),
            AmbientSoundTile(
                title = "Moodist",
                subtitle = "Open Moodist",
                url = "https://moodist.mvze.net",
                icon = Icons.Default.GraphicEq
            )
        )
    }

    val categories = remember {
        listOf(
            FocusCategory(
                id = "study",
                title = "পড়াশোনা (Study Focus)",
                icon = Icons.Default.MenuBook,
                doList = listOf(
                    "২৫–৫০ মিনিট মনোযোগ দিয়ে পড়ুন।",
                    "৫–১০ মিনিট বিরতি নিন।",
                    "ফোকাস গার্ড অন রাখুন।",
                    "একটি কাজ শেষ করে পরের কাজে যান।"
                ),
                musicList = listOf(
                    MusicItem("Piano Instrumental", "https://www.youtube.com/results?search_query=Piano+Instrumental"),
                    MusicItem("Lo-Fi (Lyrics ছাড়া)", "https://www.youtube.com/results?search_query=Lo-Fi+Study+Music"),
                    MusicItem("Rain Sounds", "https://www.youtube.com/results?search_query=Rain+Sounds+for+Studying"),
                    MusicItem("White Noise", "https://www.youtube.com/results?search_query=White+Noise")
                )
            ),
            FocusCategory(
                id = "sleep",
                title = "ঘুম (Better Sleep)",
                icon = Icons.Default.Bedtime,
                doList = listOf(
                    "প্রতিদিন একই সময়ে ঘুমান।",
                    "ঘুমের আগে স্ক্রিন ব্যবহার কমান।",
                    "চা/কফি রাতে এড়িয়ে চলুন।",
                    "ঘর শান্ত ও অন্ধকার রাখুন।"
                ),
                musicList = listOf(
                    MusicItem("Deep Sleep Music", "https://www.youtube.com/results?search_query=Deep+Sleep+Music"),
                    MusicItem("Soft Piano Sleep", "https://www.youtube.com/results?search_query=Soft+Piano+Sleep"),
                    MusicItem("Rain Sounds for Sleep", "https://www.youtube.com/results?search_query=Rain+Sounds+for+Sleep"),
                    MusicItem("Ocean Waves Sleep", "https://www.youtube.com/results?search_query=Ocean+Waves+Sleep"),
                    MusicItem("Ambient Sleep Music", "https://www.youtube.com/results?search_query=Ambient+Sleep+Music")
                )
            ),
            FocusCategory(
                id = "stress",
                title = "দুশ্চিন্তা (Stress Relief)",
                icon = Icons.Default.Spa,
                doList = listOf(
                    "ধীরে গভীর শ্বাস নিন।",
                    "৫–১০ মিনিট মেডিটেশন করুন।",
                    "চিন্তাগুলো লিখে রাখুন।",
                    "প্রতিদিন কিছুক্ষণ হাঁটুন।"
                ),
                musicList = listOf(
                    MusicItem("Relaxing Instrumental", "https://www.youtube.com/results?search_query=Relaxing+Instrumental"),
                    MusicItem("Calm Piano", "https://www.youtube.com/results?search_query=Calm+Piano"),
                    MusicItem("Nature Sounds", "https://www.youtube.com/results?search_query=Nature+Sounds"),
                    MusicItem("Meditation Music", "https://www.youtube.com/results?search_query=Meditation+Music")
                )
            ),
            FocusCategory(
                id = "anger",
                title = "রাগ (Anger Control)",
                icon = Icons.Default.SelfImprovement,
                doList = listOf(
                    "১০ সেকেন্ড বিরতি নিন।",
                    "গভীর শ্বাস নিন।",
                    "প্রয়োজনে কিছুক্ষণ স্থান পরিবর্তন করুন।",
                    "শান্ত হয়ে তারপর সিদ্ধান্ত নিন।"
                ),
                musicList = listOf(
                    MusicItem("Soft Piano", "https://www.youtube.com/results?search_query=Soft+Piano"),
                    MusicItem("Flute Music", "https://www.youtube.com/results?search_query=Relaxing+Flute+Music"),
                    MusicItem("Rain Sounds", "https://www.youtube.com/results?search_query=Rain+Sounds"),
                    MusicItem("Peaceful Instrumental", "https://www.youtube.com/results?search_query=Peaceful+Instrumental")
                )
            ),
            FocusCategory(
                id = "fear",
                title = "ভয় (Fear & Anxiety)",
                icon = Icons.Default.Psychology,
                doList = listOf(
                    "ধীরে শ্বাস নিন।",
                    "বাস্তব তথ্য দিয়ে চিন্তা যাচাই করুন।",
                    "ছোট ছোট ধাপে ভয়কে মোকাবিলা করুন।",
                    "প্রয়োজনে বিশ্বস্ত কারও সঙ্গে কথা বলুন।"
                ),
                musicList = listOf(
                    MusicItem("Meditation Music", "https://www.youtube.com/results?search_query=Meditation+Music"),
                    MusicItem("Forest Sounds", "https://www.youtube.com/results?search_query=Forest+Sounds"),
                    MusicItem("Ocean Waves", "https://www.youtube.com/results?search_query=Ocean+Waves"),
                    MusicItem("Ambient Relaxing Music", "https://www.youtube.com/results?search_query=Ambient+Relaxing+Music")
                )
            )
        )
    }

    // Keep track of expanded states, defaulted to false (collapsed)
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // TOP APP BAR - Only Title as requested
        Text(
            text = "Focus",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 3 Minimal Ambient Sound Icon Cards in 1 Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ambientSounds.forEach { tile ->
                        MinimalAmbientSoundCard(
                            tile = tile,
                            onClick = { onOpenWeb(tile.url, tile.title) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Category Expansion Accordions Header / Section Spacer
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 5 Category Expansion Cards
            items(categories, key = { it.id }) { category ->
                val isExpanded = expandedStates[category.id] ?: false
                CategoryAccordionCard(
                    category = category,
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedStates[category.id] = !isExpanded },
                    onOpenMusic = { music -> onOpenWeb(music.url, music.title) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MinimalAmbientSoundCard(
    tile: AmbientSoundTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("ambient_tile_${tile.title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenCardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C2B1D))
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.title,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryAccordionCard(
    category: FocusCategory,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenMusic: (MusicItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("category_accordion_${category.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreenSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreenCardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row - Tappable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C2B1D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = category.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Expanded Content Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DarkGreenCardBorder)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 1: যা করবেন
                    Text(
                        text = "যা করবেন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    category.doList.forEach { itemText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = itemText,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 2: শুনুন
                    Text(
                        text = "শুনুন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    category.musicList.forEach { music ->
                        MusicTileButton(
                            music = music,
                            onClick = { onOpenMusic(music) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MusicTileButton(
    music: MusicItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkGreenSurfaceSecondary)
            .border(1.dp, DarkGreenCardBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0C2B1D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = music.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "Open",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldPrimary
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppWebViewScreen(
    url: String,
    title: String,
    onClose: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenBackground)
    ) {
        // WebView Top Toolbar spanning seamlessly behind status bar
        Surface(
            color = DarkGreenSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (webViewRef?.canGoBack() == true) {
                            webViewRef?.goBack()
                        } else {
                            onClose()
                        }
                    },
                    modifier = Modifier.testTag("webview_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (title.isBlank()) "Focus Media" else title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = url,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("webview_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkGreenCardBorder)
        )

        // Android WebView Content
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        javaScriptCanOpenWindowsAutomatically = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false // Always load inside this WebView
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            return false
                        }
                    }
                    webViewRef = this
                    loadUrl(url)
                }
            },
            update = { webView ->
                webViewRef = webView
            }
        )
    }
}
