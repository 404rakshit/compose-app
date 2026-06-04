package com.deliberate.codelab.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.deliberate.codelab.domain.model.TodoItem
import com.deliberate.codelab.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarmsForHabit(habit: TodoItem) {
        if (habit.reminders.isBlank()) return

        val times = habit.reminders.split(",")
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        times.forEach { timeString ->
            try {
                // 1. Parse "08:00 AM" into a Date object
                val parsedTime = timeFormat.parse(timeString.trim()) ?: return@forEach

                // 2. Set up the Calendar for today at that exact time
                val calendar = Calendar.getInstance().apply {
                    val timeCalendar = Calendar.getInstance().apply { time = parsedTime }
                    set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    // If the time has already passed today, schedule it for tomorrow!
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1)
                    }
                }

                // 3. Create the Intent that will be broadcasted
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("EXTRA_TITLE", habit.icon + " " + habit.title)
                    putExtra("EXTRA_MESSAGE", "It's time to work on your habit!")
                }

                // 4. Create a unique Request Code so alarms don't overwrite each other
                val requestCode = (habit.id.hashCode() + timeString.hashCode())

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 5. Schedule the Exact Alarm
                // Note: To repeat daily automatically, you could use setRepeating, but setExact is more reliable on modern Android.
                // Best practice is to set an exact alarm, and then have the receiver schedule the next day's alarm when it fires.
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

                android.util.Log.d("AlarmDebug", "Scheduled alarm for ${habit.title} at ${calendar.time}")

            } catch (e: Exception) {
                android.util.Log.e("AlarmDebug", "Failed to parse time: $timeString", e)
            }
        }
    }
}