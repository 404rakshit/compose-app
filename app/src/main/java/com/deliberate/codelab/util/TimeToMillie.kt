package com.deliberate.codelab.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Takes a list of time strings (e.g., "08:00 AM") and finds the closest
 * upcoming epoch timestamp (in milliseconds) from right now.
 */
fun calculateNextTriggerTime(reminders: List<String>): Long? {
    if (reminders.isEmpty()) return null

    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val now = System.currentTimeMillis()
    var closestFutureTime: Long? = null

    for (timeString in reminders) {
        try {
            val parsedDate = sdf.parse(timeString) ?: continue
            val timeCalendar = Calendar.getInstance().apply { time = parsedDate }

            // Create a calendar for today with the parsed hours and minutes
            val targetCalendar = Calendar.getInstance()
            targetCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            targetCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            targetCalendar.set(Calendar.SECOND, 0)
            targetCalendar.set(Calendar.MILLISECOND, 0)

            // If this time has already passed today, push it to tomorrow
            if (targetCalendar.timeInMillis <= now) {
                targetCalendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val targetTime = targetCalendar.timeInMillis

            // We only want the *closest* upcoming alarm
            if (closestFutureTime == null || targetTime < closestFutureTime) {
                closestFutureTime = targetTime
            }
        } catch (e: Exception) {
            // Handle parsing errors if the string format is unexpected
            e.printStackTrace()
        }
    }

    return closestFutureTime
}