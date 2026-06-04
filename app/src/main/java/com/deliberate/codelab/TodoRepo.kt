package com.deliberate.codelab

import android.content.ContentValues
import android.content.Context
import com.deliberate.codelab.domain.model.Priority
import com.deliberate.codelab.domain.model.Repetition
import com.deliberate.codelab.domain.model.Status
import com.deliberate.codelab.domain.model.TodoItem
import com.deliberate.codelab.util.AndroidAlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class TodoRepository(private val dbHelper: TodoDatabaseHelper) {

    // --- CORE HABIT FUNCTIONS ---

    suspend fun insert(todo: TodoItem) = withContext(Dispatchers.IO) {
        try {

            android.util.Log.d("DatabaseDebug", "SUCCESS: HELLO")

            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(TodoDatabaseHelper.COLUMN_ID, todo.id.ifEmpty { UUID.randomUUID().toString() })
                put(TodoDatabaseHelper.COLUMN_TITLE, todo.title)
                put(TodoDatabaseHelper.COLUMN_STATUS, todo.status.name)
                put(TodoDatabaseHelper.COLUMN_TYPE, todo.type)
                put(TodoDatabaseHelper.COLUMN_ICON, todo.icon)
                put(TodoDatabaseHelper.COLUMN_COLOR, todo.colorArgb)
                put(TodoDatabaseHelper.COLUMN_REPEAT, todo.repeatGoal)
                put(TodoDatabaseHelper.COLUMN_CATEGORY, todo.category)
                put(TodoDatabaseHelper.COLUMN_REMINDERS, todo.reminders)
                put(TodoDatabaseHelper.COLUMN_DESC, todo.description ?: "")
                put(TodoDatabaseHelper.COLUMN_REP, todo.repetition?.name ?: Repetition.DAILY.name)
                put(TodoDatabaseHelper.COLUMN_TIME, todo.timeInMillis ?: 0L)
                put(TodoDatabaseHelper.COLUMN_PRIORITY, todo.priority?.name ?: Priority.LOW.name)
            }

            // We changed insert to insertOrThrow so it physically crashes if something is wrong
            val rowId = db.insertOrThrow(TodoDatabaseHelper.TABLE_TODOS, null, values)
            android.util.Log.d("DatabaseDebug", "SUCCESS: Habit saved to row $rowId")

            db.close()
        } catch (e: Exception) {
            android.util.Log.e("DatabaseDebug", "CRASH DURING INSERT:", e)
        }
    }

    suspend fun getAllTodos(): List<TodoItem> = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(TodoDatabaseHelper.TABLE_TODOS, null, null, null, null, null, null)
            val todos = mutableListOf<TodoItem>()

            with(cursor) {
                while (moveToNext()) {
                    todos.add(
                        TodoItem(
                            id = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID)),
                            title = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TITLE)),
                            status = Status.valueOf(getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_STATUS))),
                            type = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TYPE)),
                            icon = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ICON)),
                            colorArgb = getInt(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_COLOR)),
                            repeatGoal = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_REPEAT)),
                            category = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_CATEGORY)),
                            reminders = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_REMINDERS)),
                            description = getString(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_DESC)),
                            timeInMillis = getLong(getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TIME))
                        )
                    )
                }
            }
            cursor.close()
            db.close()

            android.util.Log.d("DatabaseDebug", "SUCCESS: Fetched ${todos.size} habits")
            return@withContext todos

        } catch (e: Exception) {
            android.util.Log.e("DatabaseDebug", "CRASH DURING FETCH:", e)
            return@withContext emptyList()
        }
    }

    suspend fun update(todo: TodoItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TodoDatabaseHelper.COLUMN_TITLE, todo.title)
            put(TodoDatabaseHelper.COLUMN_STATUS, todo.status.name)
            put(TodoDatabaseHelper.COLUMN_TIME, todo.timeInMillis ?: 0L)
            put(TodoDatabaseHelper.COLUMN_REP, todo.repetition?.name ?: Repetition.DAILY.name)
            // ... include any other fields that might change
        }

        db.update(
            TodoDatabaseHelper.TABLE_TODOS,
            values,
            "${TodoDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(todo.id)
        )
        db.close()
    }

    suspend fun updateTodoStatus(id: String, status: Status) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            // Save the enum state as a String so SQLite can read it
            put(TodoDatabaseHelper.COLUMN_STATUS, status.name)
        }
        db.update(
            TodoDatabaseHelper.TABLE_TODOS,
            values,
            "${TodoDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )
        db.close()
    }

    suspend fun deleteTodo(id: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(
            TodoDatabaseHelper.TABLE_TODOS,
            "${TodoDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )
        db.close()
    }

    // --- STREAK TRACKING FUNCTIONS ---

    suspend fun logTaskCompletion(taskId: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TodoDatabaseHelper.COLUMN_LOG_ID, UUID.randomUUID().toString())
            put(TodoDatabaseHelper.COLUMN_LOG_TASK_ID, taskId)
            put(TodoDatabaseHelper.COLUMN_LOG_COMPLETED_AT, System.currentTimeMillis())
        }
        db.insert(TodoDatabaseHelper.TABLE_LOGS, null, values)
        db.close()
    }

//    suspend fun completeTaskAndReschedule(context: Context, taskId: String) = withContext(Dispatchers.IO) {
//        // 1. Log the completion for streak calculations
//        logTaskCompletion(taskId)
//
//        // 2. Fetch the current task data
//        val todo = getTodoById(taskId) ?: return@withContext
//
//        val db = dbHelper.writableDatabase
//
//        if (todo.repetition != null && todo.repetition != Repetition.NONE) {
//            // 3a. Calculate the next occurrence
//            val nextTime = calculateNextTriggerTime(todo.timeInMillis ?: 0L, todo.repetition)
//
//            // 3b. Update DB: Bump the time and keep it PENDING
//            val values = ContentValues().apply {
//                put(TodoDatabaseHelper.COLUMN_TIME, nextTime)
//                put(TodoDatabaseHelper.COLUMN_STATUS, Status.PENDING.name)
//            }
//            db.update(TodoDatabaseHelper.TABLE_TODOS, values, "${TodoDatabaseHelper.COLUMN_ID} = ?", arrayOf(taskId))
//
//            // 3c. Routing Logic: Does this next alarm happen TODAY?
//            // (e.g., if they were way behind on a daily habit and are catching up)
//            if (isToday(nextTime)) {
//                val scheduler = AndroidAlarmScheduler(context)
//                scheduler.schedule(nextTime, todo.title)
//            } else {
//                // It's tomorrow or later.
//                // Do nothing with AlarmManager. The Daily WorkManager will pick it up on the correct day.
//                val scheduler = AndroidAlarmScheduler(context)
//                scheduler.cancel(todo.title) // Clear today's alarm just in case
//            }
//
//            android.util.Log.d("DatabaseDebug", "SUCCESS: Rescheduled repeating task to $nextTime")
//
//        } else {
//            // 4. It's a one-off task. Just mark it completed.
//            val values = ContentValues().apply {
//                put(TodoDatabaseHelper.COLUMN_STATUS, Status.COMPLETED.name)
//            }
//            db.update(TodoDatabaseHelper.TABLE_TODOS, values, "${TodoDatabaseHelper.COLUMN_ID} = ?", arrayOf(taskId))
//
//            // Cancel any pending alarms for it
//            val scheduler = AndroidAlarmScheduler(context)
//            scheduler.cancel(todo.title)
//
//            android.util.Log.d("DatabaseDebug", "SUCCESS: Marked one-off task completed")
//        }
//
//        db.close()
//    }

    // Quick helper to check if a timestamp falls within today's boundaries
    private fun isToday(timeInMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timeInMillis
        val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        return currentDay == targetDay && currentYear == targetYear
    }

    suspend fun getTodoById(id: String): TodoItem? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        var todo: TodoItem? = null

        val cursor = db.query(
            TodoDatabaseHelper.TABLE_TODOS,
            null,
            "${TodoDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            todo = TodoItem(
                id = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TITLE)),
                status = Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_STATUS))),
                type = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TYPE)),
                icon = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ICON)),
                colorArgb = cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_COLOR)),
                repeatGoal = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_REPEAT)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_CATEGORY)),
                reminders = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_REMINDERS)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_DESC)),
                timeInMillis = cursor.getLong(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TIME)),
                repetition = Repetition.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_REP)))
            )
        }
        cursor.close()
        db.close()

        return@withContext todo
    }

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

    suspend fun calculateCurrentStreak(taskId: String): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${TodoDatabaseHelper.COLUMN_LOG_COMPLETED_AT} FROM ${TodoDatabaseHelper.TABLE_LOGS} WHERE ${TodoDatabaseHelper.COLUMN_LOG_TASK_ID} = ? ORDER BY ${TodoDatabaseHelper.COLUMN_LOG_COMPLETED_AT} DESC",
            arrayOf(taskId)
        )

        val completedDays = mutableSetOf<Long>()
        if (cursor.moveToFirst()) {
            do {
                val timestamp = cursor.getLong(0)
                val dayOfEpoch = TimeUnit.MILLISECONDS.toDays(timestamp)
                completedDays.add(dayOfEpoch)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        if (completedDays.isEmpty()) return@withContext 0

        val todayDayOfEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        var currentStreak = 0

        var dayToCheck = todayDayOfEpoch
        if (!completedDays.contains(todayDayOfEpoch) && !completedDays.contains(todayDayOfEpoch - 1)) {
            return@withContext 0
        }

        while (completedDays.contains(dayToCheck) || (currentStreak == 0 && completedDays.contains(dayToCheck - 1))) {
            if (completedDays.contains(dayToCheck)) {
                currentStreak++
            }
            dayToCheck--
        }

        return@withContext currentStreak
    }
}