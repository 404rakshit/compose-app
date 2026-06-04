package com.deliberate.codelab.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.deliberate.codelab.util.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmDebug", "RECEIVER FIRED!")

        val message = intent.getStringExtra("ALARM_MESSAGE") ?: "Time to wake up!"

        // 1. Prepare your Activity Intent
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ALARM_MESSAGE", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // 2. Wrap it in a PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 3. Create the Notification Channel (Strictly required for Android 8+)
        val channelId = "HABIT_ALARM_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH // 🚨 MUST be HIGH for full-screen intents
            ).apply {
                description = "Fires when a habit reminder is due"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 4. Build the Notification with the Full Screen Intent attached
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            // Replace this with your actual app icon (e.g., R.drawable.ic_launcher_foreground)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Habit Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            // 🚨 THIS IS THE MAGIC LINE: Tells OS to launch the Activity if the screen is off
            .setFullScreenIntent(pendingIntent, true)

        // 5. Fire it off!
        Log.d("AlarmDebug", "Firing Full Screen Notification...")
        notificationManager.notify(message.hashCode(), notificationBuilder.build())
    }
}