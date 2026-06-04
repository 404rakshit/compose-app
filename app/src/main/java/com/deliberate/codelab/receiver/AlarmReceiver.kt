package com.deliberate.codelab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.deliberate.codelab.util.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("AlarmDebug", "RECEIVER FIRED!")

        val message = intent.getStringExtra("ALARM_MESSAGE") ?: "Time to wake up!"

        // When the alarm fires, we immediately launch the Full Screen Activity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ALARM_MESSAGE", message)
            // FLAG_ACTIVITY_NEW_TASK is strictly required when launching an Activity from a Receiver
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Log.d("AlarmDebug", "RECEIVER FIRED 2!")

        context.startActivity(alarmIntent)
    }
}