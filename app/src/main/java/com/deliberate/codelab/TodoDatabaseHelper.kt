package com.deliberate.codelab

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TodoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "todos.db"
        const val DATABASE_VERSION = 3 // <-- Bumped to 2 to trigger the upgrade

        const val TABLE_TODOS = "todos"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESC = "description"
        const val COLUMN_REP = "repetition"
        const val COLUMN_TIME = "time_in_millis"
        const val COLUMN_STATUS = "status"
        const val COLUMN_PRIORITY = "priority"

        const val TABLE_LOGS = "task_logs"
        const val COLUMN_LOG_ID = "log_id"
        const val COLUMN_LOG_TASK_ID = "task_id"
        const val COLUMN_LOG_COMPLETED_AT = "completed_at"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TODOS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESC TEXT,
                $COLUMN_REP TEXT NOT NULL,
                $COLUMN_TIME INTEGER,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_PRIORITY TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        val createLogsTableQuery = """
            CREATE TABLE $TABLE_LOGS (
                $COLUMN_LOG_ID TEXT PRIMARY KEY,
                $COLUMN_LOG_TASK_ID TEXT NOT NULL,
                $COLUMN_LOG_COMPLETED_AT INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_LOG_TASK_ID) REFERENCES $TABLE_TODOS(${COLUMN_ID}) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createLogsTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TODOS")
        onCreate(db)
    }
}