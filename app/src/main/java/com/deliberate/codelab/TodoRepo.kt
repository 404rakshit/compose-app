package com.deliberate.codelab

import android.content.ContentValues

class TodoRepository(private val dbHelper: TodoDatabaseHelper) {

    // Read: Map raw Cursor rows to Domain Objects
    fun getAllTodos(): List<TodoItem> {
        val todoList = mutableListOf<TodoItem>()
        val db = dbHelper.readableDatabase

        // Execute raw SQL
        val cursor = db.rawQuery("SELECT * FROM ${TodoDatabaseHelper.TABLE_TODOS}", null)

        // Iterate through the raw rows
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TITLE))
                val isCompletedInt = cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_IS_COMPLETED))

                todoList.add(TodoItem(id, title, isCompletedInt == 1))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return todoList
    }

    // Create: Using ContentValues (Android's safe parameter binding) to prevent SQL injection
    fun insertTodo(todo: TodoItem) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TodoDatabaseHelper.COLUMN_ID, todo.id)
            put(TodoDatabaseHelper.COLUMN_TITLE, todo.title)
            put(TodoDatabaseHelper.COLUMN_IS_COMPLETED, if (todo.isCompleted) 1 else 0)
        }
        db.insert(TodoDatabaseHelper.TABLE_TODOS, null, values)
        db.close()
    }

    // Update
    fun updateTodoStatus(id: String, isCompleted: Boolean) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TodoDatabaseHelper.COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        db.update(
            TodoDatabaseHelper.TABLE_TODOS,
            values,
            "${TodoDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )
        db.close()
    }

    // Delete
    fun deleteTodo(id: String) {
        val db = dbHelper.writableDatabase
        db.delete(
            TodoDatabaseHelper.TABLE_TODOS,
            "${TodoDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )
        db.close()
    }
}