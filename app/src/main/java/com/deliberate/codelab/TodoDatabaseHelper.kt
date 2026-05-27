package com.deliberate.codelab

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TodoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "habits.db"
        // Bump to version 3 to apply these massive merged changes safely
        const val DATABASE_VERSION = 3

        // --- TODOS TABLE ---
        const val TABLE_TODOS = "todos"
        const val TABLE_NAME = "todos" // Pointing to the same table to fix your repo's mixed usage

        // Primary & Status
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_STATUS = "status"

        // New Habit Features
        const val COLUMN_TYPE = "type"
        const val COLUMN_ICON = "icon"
        const val COLUMN_COLOR = "color"
        const val COLUMN_REPEAT = "repeatGoal"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_REMINDERS = "reminders"

        // Legacy/Additional Features
        const val COLUMN_DESC = "description"
        const val COLUMN_REP = "repetition"
        const val COLUMN_TIME = "timeInMillis"
        const val COLUMN_PRIORITY = "priority"

        // --- LOGS TABLE (For Streaks) ---
        const val TABLE_LOGS = "logs"
        const val COLUMN_LOG_ID = "log_id"
        const val COLUMN_LOG_TASK_ID = "task_id"
        const val COLUMN_LOG_COMPLETED_AT = "completed_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTodosTable = """
            CREATE TABLE $TABLE_TODOS (
                $COLUMN_ID TEXT PRIMARY KEY, 
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_TYPE TEXT,
                $COLUMN_ICON TEXT,
                $COLUMN_COLOR INTEGER,
                $COLUMN_REPEAT TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_REMINDERS TEXT,
                $COLUMN_DESC TEXT,
                $COLUMN_REP TEXT,
                $COLUMN_TIME INTEGER,
                $COLUMN_PRIORITY TEXT
            )
        """.trimIndent()

        val createLogsTable = """
            CREATE TABLE $TABLE_LOGS (
                $COLUMN_LOG_ID TEXT PRIMARY KEY,
                $COLUMN_LOG_TASK_ID TEXT NOT NULL,
                $COLUMN_LOG_COMPLETED_AT INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_LOG_TASK_ID) REFERENCES $TABLE_TODOS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createTodosTable)
        db.execSQL(createLogsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TODOS")
        onCreate(db)
    }
}