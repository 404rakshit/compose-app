package com.deliberate.codelab

import android.content.ContentValues
import com.deliberate.codelab.domain.model.Priority
import com.deliberate.codelab.domain.model.Repetition
import com.deliberate.codelab.domain.model.Status
import com.deliberate.codelab.domain.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

class TodoRepository(private val dbHelper: TodoDatabaseHelper) {

    // withContext(Dispatchers.IO) forces this block to run on a background thread
    suspend fun getAllTodos(): List<TodoItem> = withContext(Dispatchers.IO) {
        val todoList = mutableListOf<TodoItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${TodoDatabaseHelper.TABLE_TODOS}", null)

        if (cursor.moveToFirst()) {
            do {
                // Read from DB and parse Enum strings back to actual Kotlin Enums safely
                val item = TodoItem(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_DESC)) ?: "",
                    repetition = runCatching { Repetition.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_REP))) }.getOrDefault(Repetition.NONE),

                    // Time can be null, so we check if the cursor is null first
                    timeInMillis = if (cursor.isNull(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TIME))) null
                    else cursor.getLong(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TIME)),

                    status = runCatching { Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_STATUS))) }.getOrDefault(Status.PENDING),
                    priority = runCatching { Priority.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_PRIORITY))) }.getOrDefault(Priority.MEDIUM)
                )
                todoList.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        todoList
    }

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

    suspend fun updateTodoStatus(id: String, status: Status) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            // We must save the Enum as a String (.name) so SQLite can read it
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

    // Delete the Task
    suspend fun deleteTodo(id: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(
            TodoDatabaseHelper.TABLE_TODOS,
            "${TodoDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )
        db.close()
    }

    suspend fun calculateCurrentStreak(taskId: String): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${TodoDatabaseHelper.COLUMN_LOG_COMPLETED_AT} FROM ${TodoDatabaseHelper.TABLE_LOGS} WHERE ${TodoDatabaseHelper.COLUMN_LOG_TASK_ID} = ? ORDER BY ${TodoDatabaseHelper.COLUMN_LOG_COMPLETED_AT} DESC",
            arrayOf(taskId)
        )

        val completedDays = mutableSetOf<Long>() // Use a Set to ignore multiple completions on the same day

        if (cursor.moveToFirst()) {
            do {
                val timestamp = cursor.getLong(0)
                // Flatten the timestamp to just "Days since epoch" so we can easily compare distinct calendar days
                val dayOfEpoch = TimeUnit.MILLISECONDS.toDays(timestamp)
                completedDays.add(dayOfEpoch)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        if (completedDays.isEmpty()) return@withContext 0

        val todayDayOfEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        var currentStreak = 0

        // Check if the streak is alive (they completed it today or yesterday)
        var dayToCheck = todayDayOfEpoch
        if (!completedDays.contains(todayDayOfEpoch) && !completedDays.contains(todayDayOfEpoch - 1)) {
            return@withContext 0 // Streak broken!
        }

        // Count backwards to find consecutive days
        while (completedDays.contains(dayToCheck) || (currentStreak == 0 && completedDays.contains(dayToCheck - 1))) {
            if (completedDays.contains(dayToCheck)) {
                currentStreak++
            }
            dayToCheck--
        }

        return@withContext currentStreak
    }

    suspend fun insertTodo(todo: TodoItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TodoDatabaseHelper.COLUMN_ID, todo.id)
            put(TodoDatabaseHelper.COLUMN_TITLE, todo.title)
            put(TodoDatabaseHelper.COLUMN_DESC, todo.description)
            put(TodoDatabaseHelper.COLUMN_REP, todo.repetition.name) // Save Enum as String
            put(TodoDatabaseHelper.COLUMN_TIME, todo.timeInMillis)
            put(TodoDatabaseHelper.COLUMN_STATUS, todo.status.name)
            put(TodoDatabaseHelper.COLUMN_PRIORITY, todo.priority.name)
        }
        db.insert(TodoDatabaseHelper.TABLE_TODOS, null, values)
        db.close()
    }

//    suspend fun updateTodoStatus(id: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
//        val db = dbHelper.writableDatabase
//        val values = ContentValues().apply {
//            put(TodoDatabaseHelper.COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
//        }
//        db.update(
//            TodoDatabaseHelper.TABLE_TODOS,
//            values,
//            "${TodoDatabaseHelper.COLUMN_ID} = ?",
//            arrayOf(id)
//        )
//        db.close()
//    }

//    suspend fun deleteTodo(id: String) = withContext(Dispatchers.IO) {
//        val db = dbHelper.writableDatabase
//        db.delete(
//            TodoDatabaseHelper.TABLE_TODOS,
//            "${TodoDatabaseHelper.COLUMN_ID} = ?",
//            arrayOf(id)
//        )
//        db.close()
//    }
}