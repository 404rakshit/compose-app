package com.deliberate.codelab.util

import android.content.Intent
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

        // Keep the screen awake while the UI is visible
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Notice: playAlarmSound() is GONE. The Service is already playing it!

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

    private fun turnOffAlarmAndFinish() {
        // Send an Intent to stop the Service (which stops the sound AND kills the notification)
        val stopIntent = Intent(this, HabitAlarmService::class.java)
        stopService(stopIntent)

        finish()
    }

    // Notice: onDestroy() cleaning up MediaPlayer is also GONE!
}