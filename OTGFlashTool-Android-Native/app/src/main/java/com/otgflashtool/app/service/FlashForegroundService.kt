package com.otgflashtool.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.otgflashtool.app.MainActivity
import com.otgflashtool.app.R

/**
 * Google Play-compliant Foreground Service (dataSync) with live progress notification & partial WakeLock.
 * Keeps long USB OTG firmware transfers alive so Android does not kill the process mid-flash.
 */
class FlashForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            releaseWakeLock()
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "انتقال فایل از طریق USB OTG"
        val detail = intent?.getStringExtra(EXTRA_DETAIL) ?: "کابل OTG را تا پایان عملیات جدا نکنید"
        val progress = intent?.getIntExtra(EXTRA_PROGRESS, -1) ?: -1

        acquireWakeLock()
        val notification = buildNotification(title, detail, progress)
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceType)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OTGFlashTool::UsbTransferLock").apply {
            setReferenceCounted(false)
            acquire(60 * 60 * 1000L) // Max 60 minutes safety cap
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
        }
        wakeLock = null
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "عملیات انتقال USB OTG",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نمایش پیشرفت انتقال فایل و جلوگیری از توقف برنامه حین فلش"
                setSound(null, null)
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, detail: String, progress: Int): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val piFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 100, openIntent, piFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_launcher)
            .setContentTitle(title)
            .setContentText(detail)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .apply {
                if (progress in 0..100) {
                    setProgress(100, progress, false)
                } else {
                    setProgress(0, 0, true)
                }
            }
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "otg_flash_transfer_channel"
        private const val NOTIFICATION_ID = 4041
        private const val ACTION_UPDATE = "com.otgflashtool.app.action.UPDATE_FLASH"
        private const val ACTION_STOP = "com.otgflashtool.app.action.STOP_FLASH"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_DETAIL = "extra_detail"
        private const val EXTRA_PROGRESS = "extra_progress"

        fun update(context: Context, title: String, detail: String, progress: Int) {
            val intent = Intent(context, FlashForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_DETAIL, detail)
                putExtra(EXTRA_PROGRESS, progress)
            }
            runCatching { ContextCompat.startForegroundService(context, intent) }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FlashForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
        }
    }
}
