package com.deliberate.codelab.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.deliberate.codelab.R // Ensure you import your R file for icons

class HabitAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val NOTIFICATION_ID = 1001

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_DISMISS = "ACTION_DISMISS"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val message = intent?.getStringExtra("ALARM_MESSAGE") ?: "Time for your habit!"

        when (action) {
            ACTION_DISMISS -> {
                Log.d("AlarmService", "Dismiss clicked from notification.")
                stopSelf() // This immediately triggers onDestroy() and kills the sound
            }
            ACTION_SNOOZE -> {
                Log.d("val channelId = \"HABIT_ALARM_CHANNEL_V5\" \n" +
                        "\n" +
                        "if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {\n" +
                        "    val channel = NotificationChannel(\n" +
                        "        channelId, \n" +
                        "        \"Habit Reminders\", \n" +
                        "        NotificationManager.IMPORTANCE_HIGH // Still required\n" +
                        "    ).apply {\n" +
                        "        description = \"Fires when a habit reminder is due\"\n" +
                        "        \n" +
                        "        // \uD83D\uDEA8 ADD THESE THREE LINES: \n" +
                        "        // This is what actually triggers the Heads-Up banner on strict devices\n" +
                        "        enableVibration(true)\n" +
                        "        vibrationPattern = longArrayOf(0, 500, 500, 500) // Explicit pattern\n" +
                        "        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC\n" +
                        "    }\n" +
                        "    notificationManager.createNotificationChannel(channel)\n" +
                        "}", "Snooze clicked from notification.")

                // 1. You would call your AndroidAlarmScheduler here to set a new alarm
                // for 10 minutes in the future.
                // val scheduler = AndroidAlarmScheduler(applicationContext)
                // scheduler.schedule(System.currentTimeMillis() + (10 * 60 * 1000), message)

                stopSelf() // Kill the current ringing sound
            }
            else -> {
                // Normal Start Flow
                startAlarm(message)
            }
        }

        return START_NOT_STICKY
    }

    private fun startAlarm(message: String) {
        playAlarmSound()

        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("ALARM_MESSAGE", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            message.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- 🚨 NEW: Create PendingIntents for the Action Buttons ---

        val dismissIntent = Intent(this, HabitAlarmService::class.java).apply {
            action = ACTION_DISMISS
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            1, // Unique request code
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, HabitAlarmService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("ALARM_MESSAGE", message) // Pass message back for rescheduling
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            2, // Unique request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "HABIT_ALARM_CHANNEL_V5"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH // Still required
            ).apply {
                description = "Fires when a habit reminder is due"

                // 🚨 ADD THESE THREE LINES:
                // This is what actually triggers the Heads-Up banner on strict devices
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500) // Explicit pattern
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // --- 🚨 NEW: Build the Advanced Notification ---
        val notification = NotificationCompat.Builder(this, channelId)
            // 1. Visuals
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System alarm icon
            .setContentTitle("Habit Reminder")
            .setContentText(message)

            // 2. Priority & Intrusion
            .setPriority(NotificationCompat.PRIORITY_MAX) // MAX ensures it forces the drop-down
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)

            // 3. The Live Timer (Chronometer)
            .setUsesChronometer(true)
            .setWhen(System.currentTimeMillis()) // Starts counting up from RIGHT NOW
            .setShowWhen(true)

            // 4. The Action Buttons
            // Note: Modern Android often ignores the icon parameter in actions, but it's required by the API.
            // Replace android.R.drawable... with your own custom drawables if you have them.
            .addAction(android.R.drawable.ic_popup_sync, "Snooze", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)

            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun playAlarmSound() {
        try {
            var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            mediaPlayer = MediaPlayer.create(this, alarmUri).apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to play sound", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}