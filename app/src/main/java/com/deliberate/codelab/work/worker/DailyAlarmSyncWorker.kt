package com.deliberate.codelab.work.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.deliberate.codelab.TodoDatabaseHelper
import com.deliberate.codelab.util.AndroidAlarmScheduler
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyAlarmSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dbHelper = TodoDatabaseHelper(applicationContext)
        val alarmScheduler = AndroidAlarmScheduler(applicationContext)
        val db = dbHelper.readableDatabase

        // 1. Calculate the start and end of "Today" in milliseconds
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfToday = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfToday = calendar.timeInMillis

        // 2. Query raw SQLite for tasks falling in today's window
        val selection = "${TodoDatabaseHelper.COLUMN_TIME} >= ? AND ${TodoDatabaseHelper.COLUMN_TIME} <= ?"
        val selectionArgs = arrayOf(startOfToday.toString(), endOfToday.toString())

        val cursor = db.query(
            TodoDatabaseHelper.TABLE_TODOS,
            arrayOf(TodoDatabaseHelper.COLUMN_ID, TodoDatabaseHelper.COLUMN_TITLE, TodoDatabaseHelper.COLUMN_TIME),
            selection,
            selectionArgs,
            null, null, null
        )

        // 3. Iterate and schedule
        cursor.use {
            while (it.moveToNext()) {
                val title = it.getString(it.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TITLE))
                val timeInMillis = it.getLong(it.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TIME))

                // Only schedule if the time hasn't already passed today
                if (timeInMillis > System.currentTimeMillis()) {
                    alarmScheduler.schedule(timeInMillis, title)
                }
            }
        }

        return Result.success()
    }
}

fun setupDailyAlarmSync(context: Context) {
    val syncRequest = PeriodicWorkRequestBuilder<DailyAlarmSyncWorker>(24, TimeUnit.HOURS)
        // Optional: Add constraints so it runs efficiently (e.g., while charging overnight)
        // .setConstraints(Constraints.Builder().setRequiresCharging(true).build())
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "DAILY_ALARM_SYNC",
        ExistingPeriodicWorkPolicy.KEEP, // Don't overwrite if it's already scheduled
        syncRequest
    )
}