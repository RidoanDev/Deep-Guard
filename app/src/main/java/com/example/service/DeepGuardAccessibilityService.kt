package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.DeepGuardDatabase
import com.example.ui.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DeepGuardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var isTimerActive = false
    private var timerEndTimeMs = 0L
    private var blockedPackageNames = setOf<String>()
    private var isPornBlocked = true
    private var isGamblingBlocked = true
    private var isDatingBlocked = true
    private var isVpnBlocked = true

    // Common browser package names
    private val browserPackages = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "com.sec.android.app.sbrowser",
        "com.brave.browser",
        "com.ucmobile.intl",
        "com.opera.mini.native"
    )

    // Whitelisted safe productivity & focus domains
    private val whitelistedDomains = listOf(
        "noisli.com", "noisli", "brain.fm", "coffitivity", "lofi.co",
        "rainymood.com", "rainymood", "asoftmurmur.com", "asoftmurmur",
        "moodist", "mvze.net", "youtube.com", "youtube"
    )

    // Blocked adult / gambling / dating / VPN keywords for URL and app package scanning
    private val adultKeywords = listOf(
        "porn", "xvideo", "xnxx", "redtube", "youporn", "xhamster", "brazzers",
        "erotic", "nude", "adult"
    )
    private val gamblingKeywords = listOf(
        "bet365", "1xbet", "melbet", "parimatch", "casino", "poker",
        "gambling", "betting", "bwin", "dafabet"
    )
    private val datingKeywords = listOf(
        "tinder", "badoo", "bumble", "hinge", "okcupid", "tan-tan", "match.com", "grindr"
    )
    private val vpnKeywords = listOf(
        "expressvpn", "nordvpn", "surfshark", "cyberghost", "protonvpn",
        "turbo.vpn", "supervpn", "vpnmaster", "freevpn"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        val db = DeepGuardDatabase.getInstance(applicationContext)
        val dao = db.appDao()

        serviceScope.launch {
            dao.getGuardSettings().collectLatest { settings ->
                if (settings != null) {
                    isTimerActive = settings.isTimerActive
                    timerEndTimeMs = settings.timerEndTimeMs
                    isPornBlocked = settings.isAdultPornBlocked
                    isGamblingBlocked = settings.isGamblingBlocked
                    isDatingBlocked = settings.isDatingBlocked
                    isVpnBlocked = settings.isVpnBypassBlocked

                    // Check if timer expired
                    if (isTimerActive && System.currentTimeMillis() >= timerEndTimeMs) {
                        dao.updateTimerState(isActive = false, endTimeMs = 0L)
                        isTimerActive = false
                    }
                }
            }
        }

        serviceScope.launch {
            dao.getBlockedApps().collectLatest { apps ->
                blockedPackageNames = apps.map { it.packageName }.toSet()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val packageName = event.packageName?.toString() ?: return

        // Skip checking DeepGuard itself
        if (packageName == applicationContext.packageName) return

        // 1. Check if App is Blocked during active Focus Timer
        if (isTimerActive && System.currentTimeMillis() < timerEndTimeMs) {
            if (blockedPackageNames.contains(packageName)) {
                blockAndRedirect(packageName, "ফোকাস টাইমার চলাকালীন $packageName খোলা নিষেধ!", showOverlay = false)
                return
            }
        }

        // 2. Strict Default Rule Blocking (Porn, Gambling, Dating, VPN apps & URLs)
        val lowerPkg = packageName.lowercase()

        val isAdultApp = isPornBlocked && adultKeywords.any { lowerPkg.contains(it) }
        val isGamblingApp = isGamblingBlocked && gamblingKeywords.any { lowerPkg.contains(it) }
        val isDatingApp = isDatingBlocked && datingKeywords.any { lowerPkg.contains(it) }
        val isVpnApp = isVpnBlocked && vpnKeywords.any { lowerPkg.contains(it) }

        if (isAdultApp || isGamblingApp || isDatingApp || isVpnApp) {
            val reason = when {
                isAdultApp -> "এডাল্ট কন্টেন্ট বা পর্ন অ্যাপ সম্পূর্ণ ব্লক করা হয়েছে!"
                isGamblingApp -> "জুয়া এবং বাজি ধরার অ্যাপ স্থায়ীভাবে ব্লকড!"
                isDatingApp -> "ডেটিং ও এডাল্ট চ্যাট অ্যাপ নিরাপদ সুরক্ষায় ব্লকড!"
                else -> "ভিপিএন বাইপাস সার্ভিস স্থায়ীভাবে ব্লক করা হয়েছে!"
            }
            blockAndRedirect(packageName, reason)
            return
        }

        // 3. Browser URL / Search Query Inspection
        if (browserPackages.contains(packageName) && rootInActiveWindow != null) {
            checkBrowserUrl(rootInActiveWindow, packageName)
        }
    }

    private fun checkBrowserUrl(nodeInfo: AccessibilityNodeInfo?, browserPkg: String) {
        nodeInfo ?: return

        // Look for URL address bars
        val textNodes = mutableListOf<String>()
        extractTextFromNodes(nodeInfo, textNodes)

        // Bypasses check if browser is displaying a whitelisted safe productivity site
        val isWhitelisted = textNodes.any { nodeText ->
            val lower = nodeText.lowercase()
            whitelistedDomains.any { domain -> lower.contains(domain) }
        }
        if (isWhitelisted) return

        for (text in textNodes) {
            val lowerText = text.lowercase()
            if (lowerText.length < 3) continue

            val containsAdult = isPornBlocked && adultKeywords.any { lowerText.contains(it) }
            val containsGambling = isGamblingBlocked && gamblingKeywords.any { lowerText.contains(it) }
            val containsDating = isDatingBlocked && datingKeywords.any { lowerText.contains(it) }
            val containsVpn = isVpnBlocked && vpnKeywords.any { lowerText.contains(it) }

            if (containsAdult || containsGambling || containsDating || containsVpn) {
                val categoryReason = when {
                    containsAdult -> "ব্রাউজারে এডাল্ট/পর্ন ওয়েবসাইট বা সার্চ ব্লক করা হয়েছে!"
                    containsGambling -> "ব্রাউজারে জুয়া বা ক্যাসিনো ইউআরএল ব্লকড!"
                    containsDating -> "ব্রাউজারে ডেটিং ওয়েবসাইট অ্যাক্সেস ব্লকড!"
                    else -> "ভিপিএন ও প্রক্সি বাইপাস ইউআরএল বন্ধ রাখা হয়েছে!"
                }
                blockAndRedirect(browserPkg, categoryReason)
                break
            }
        }
    }

    private fun extractTextFromNodes(node: AccessibilityNodeInfo, textList: MutableList<String>) {
        if (node.text != null && node.text.isNotBlank()) {
            textList.add(node.text.toString())
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                extractTextFromNodes(child, textList)
                child.recycle()
            }
        }
    }

    private fun blockAndRedirect(packageName: String, reason: String, showOverlay: Boolean = true) {
        // Go to Android Home immediately
        performGlobalAction(GLOBAL_ACTION_HOME)

        // Increment block counter in DB
        serviceScope.launch {
            val db = DeepGuardDatabase.getInstance(applicationContext)
            db.appDao().incrementBlockedCount()
        }

        if (showOverlay) {
            // Launch full-screen BlockOverlayActivity
            val overlayIntent = Intent(this, BlockOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("EXTRA_BLOCKED_PACKAGE", packageName)
                putExtra("EXTRA_BLOCK_REASON", reason)
            }
            startActivity(overlayIntent)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
    }
}
