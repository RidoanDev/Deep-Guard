package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.DeepGuardDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DeepGuardForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val NOTIFICATION_ID = 8801
    private val CHANNEL_ID = "deepguard_service_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("DeepGuard সিকিউরিটি শীল্ড সক্রিয় রয়েছে", "সকল অ্যাপ ও অনলাইন সেফটি মনিটর করা হচ্ছে")
        startForeground(NOTIFICATION_ID, notification)

        val dao = DeepGuardDatabase.getInstance(applicationContext).appDao()

        serviceScope.launch {
            dao.getGuardSettings().collectLatest { settings ->
                if (settings != null && settings.isTimerActive) {
                    val remainingMs = settings.timerEndTimeMs - System.currentTimeMillis()
                    if (remainingMs > 0) {
                        val minutes = (remainingMs / 1000) / 60
                        val seconds = (remainingMs / 1000) % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)
                        updateNotification("ফোকাস টাইমার লক সক্রিয়: $timeStr বাকি", "লক করা অ্যাপসমূহ অ্যাক্সেস করা যাবে না")
                    } else {
                        dao.updateTimerState(isActive = false, endTimeMs = 0L)
                        updateNotification("DeepGuard শীল্ড সক্রিয়", "টাইমার শেষ হয়েছে। অ্যাপ সুরক্ষা প্রস্তুত।")
                    }
                } else {
                    updateNotification("DeepGuard শীল্ড সক্রিয়", "পর্ন, জুয়া, ডেটিং ও ভিপিএন অটোমেটিক ব্লকড")
                }
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DeepGuard Guard Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DeepGuard ব্যাকগ্রাউন্ড সিকিউরিটি স্টেটাস নির্দেশক"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(title: String, text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    private fun buildNotification(title: String, text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
