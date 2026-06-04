package com.deliberate.codelab.util

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚨 1. PUNCH THROUGH THE LOCK SCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Keep the screen awake while the alarm is ringing
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 🚨 2. START THE ALARM SOUND
        playAlarmSound()

        val message = intent.getStringExtra("ALARM_MESSAGE") ?: "Time for your habit!"

        setContent {
            // Your Compose UI for the Alarm Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = message)
                Button(onClick = { turnOffAlarmAndFinish() }) {
                    Text("Dismiss")
                }
            }
        }
    }

    private fun playAlarmSound() {
        try {
            // Get the user's default system alarm sound
            var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                // Fallback to standard notification sound if no alarm is set
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer.create(this, alarmUri).apply {
                isLooping = true // Keep ringing until dismissed!
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun turnOffAlarmAndFinish() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Remove the notification from the tray if they dismiss from the Activity
        // val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // notificationManager.cancel(MESSAGE_HASHCODE_IF_YOU_PASSED_IT)

        finish() // Close the activity and let the user return to their lock screen
    }

    override fun onDestroy() {
        super.onDestroy()
        // Safety catch in case the OS destroys the activity (e.g., user swipes it away)
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}