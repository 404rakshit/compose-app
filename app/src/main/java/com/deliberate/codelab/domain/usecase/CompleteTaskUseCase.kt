package com.deliberate.codelab.domain.usecase

import com.deliberate.codelab.TodoRepository
import com.deliberate.codelab.domain.model.Repetition
import com.deliberate.codelab.domain.model.Status
import com.deliberate.codelab.util.AndroidAlarmScheduler
import java.util.Calendar

class CompleteTaskUseCase(
    private val repository: TodoRepository,
    private val alarmScheduler: AndroidAlarmScheduler
) {
    suspend operator fun invoke(taskId: String) {
        // 1. Fetch the exact state of the task from the DB
        val todo = repository.getTodoById(taskId) ?: return

        // 2. Log completion for your streak calculations
        repository.logTaskCompletion(taskId)

        // 3. Routing Logic: Does it repeat or is it a one-off?
        if (todo.repetition != null && todo.repetition != Repetition.NONE) {

            // It repeats. Calculate the next exact timestamp.
            val nextTime = calculateNextTriggerTime(todo.timeInMillis ?: 0L, todo.repetition)

            // Create a copy of the item with the new time, keeping it PENDING
            val updatedTodo = todo.copy(
                timeInMillis = nextTime,
                status = Status.PENDING
            )

            // Save the updated future task to the database
            repository.update(updatedTodo)

            // 4. Alarm Manager Routing
            if (isToday(nextTime)) {
                // E.g., The user is catching up on past-due habits and the next one is still today
                alarmScheduler.schedule(nextTime, updatedTodo.title)
            } else {
                // The next occurrence is tomorrow or later. Cancel today's alarm.
                // The Daily WorkManager will schedule this when the correct day arrives.
                alarmScheduler.cancel(todo.title)
            }

        } else {
            // It is a single-use task. Mark it completely done.
            val updatedTodo = todo.copy(status = Status.COMPLETED)
            repository.update(updatedTodo)

            // Clear any system alarms associated with it
            alarmScheduler.cancel(todo.title)
        }
    }

    // --- DOMAIN MATH HELPERS ---

    private fun calculateNextTriggerTime(originalTimeInMillis: Long, repetition: Repetition): Long {
        if (originalTimeInMillis == 0L) return 0L

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = originalTimeInMillis

        // Calendar safely handles leap years, month lengths, etc.
        when (repetition) {
            Repetition.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            Repetition.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            Repetition.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            Repetition.NONE -> {}
        }
        return calendar.timeInMillis
    }

    private fun isToday(timeInMillis: Long): Boolean {
        if (timeInMillis == 0L) return false

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