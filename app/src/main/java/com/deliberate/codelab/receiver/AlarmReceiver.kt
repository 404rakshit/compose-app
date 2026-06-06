package com.deliberate.codelab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.deliberate.codelab.util.HabitAlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("ALARM_MESSAGE") ?: "Time to wake up!"

        val serviceIntent = Intent(context, HabitAlarmService::class.java).apply {
            action = HabitAlarmService.ACTION_START // Explicitly tell it to start
            putExtra("ALARM_MESSAGE", message)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}