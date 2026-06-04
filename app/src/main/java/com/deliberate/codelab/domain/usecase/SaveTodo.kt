package com.deliberate.codelab.domain.usecase

import android.util.Log
import com.deliberate.codelab.TodoRepository
import com.deliberate.codelab.domain.model.TodoItem
import com.deliberate.codelab.util.AndroidAlarmScheduler
import java.util.Calendar

class SaveTodoUseCase(
    private val repository: TodoRepository,
    private val alarmScheduler: AndroidAlarmScheduler
) {
    // Overriding the invoke operator allows you to call the class like a function
    suspend operator fun invoke(todo: TodoItem) {
        // 1. Save to the database
        repository.insert(todo)

        // 2. Extract the time
        val triggerTime = todo.timeInMillis ?: return

        Log.d("AlarmDebug", "Scheduling for Epoch: $triggerTime | Current Epoch: ${System.currentTimeMillis()}")

        // 3. Routing Logic: Schedule immediately if it's happening today
        if (isToday(triggerTime)) {
            alarmScheduler.schedule(triggerTime, todo.title)
        }
    }

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
}