package com.deliberate.codelab.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.deliberate.codelab.receiver.AlarmReceiver

class AndroidAlarmScheduler(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(triggerTimeMillis: Long, message: String) {
        Log.d("AlarmDebug", "Scheduler called for: $message")

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_MESSAGE", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            message.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e("AlarmDebug", "ABORT: canScheduleExactAlarms() is FALSE. Permission missing!")
            return
        }

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

        Log.d("AlarmDebug", "SUCCESS: Handed over to OS AlarmManager")
    }

    fun cancel(message: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            message.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}