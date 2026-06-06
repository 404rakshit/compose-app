package com.deliberate.codelab.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.deliberate.codelab.TodoDatabaseHelper
import com.deliberate.codelab.TodoRepository
import com.deliberate.codelab.util.AndroidAlarmScheduler
import java.util.Calendar

class DailyAlarmSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WorkManagerDebug", "Executing optimized daily alarm sync...")

        val appContext = applicationContext
        val dbHelper = TodoDatabaseHelper(appContext)
        val repository = TodoRepository(dbHelper)
        val alarmScheduler = AndroidAlarmScheduler(appContext)

        // 1. Start time is RIGHT NOW (don't schedule missed alarms)
        val now = System.currentTimeMillis()

        // 2. End time is 23:59:59 tonight
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfToday = calendar.timeInMillis

        try {
            // 3. Let SQLite do all the heavy lifting!
            val todaysTodos = repository.getTodosForDateRange(now, endOfToday)

            // 4. Schedule the lean results
            todaysTodos.forEach { todo ->
                val triggerTime = todo.timeInMillis ?: return@forEach

                alarmScheduler.schedule(triggerTime, todo.title)
                Log.d("WorkManagerDebug", "Successfully queued alarm for: ${todo.title} at $triggerTime")
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e("WorkManagerDebug", "Sync crashed!", e)
            return Result.retry()
        }
    }
}