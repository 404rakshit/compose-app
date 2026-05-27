package com.deliberate.codelab

import android.content.ContentValues
import com.deliberate.quickalarm.domain.model.Priority
import com.deliberate.quickalarm.domain.model.Repetition
import com.deliberate.quickalarm.domain.model.Status
import com.deliberate.quickalarm.domain.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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