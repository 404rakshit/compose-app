package com.deliberate.codelab

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliberate.codelab.ui.screens.HabitDraft // Make sure this matches where you put HabitDraft!
import com.deliberate.codelab.domain.model.Status
import com.deliberate.codelab.domain.model.TodoItem
import com.deliberate.codelab.domain.usecase.CompleteTaskUseCase
import com.deliberate.codelab.domain.usecase.SaveTodoUseCase
import com.deliberate.codelab.util.AndroidAlarmScheduler
import com.deliberate.codelab.util.calculateNextTriggerTime
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodoViewModel(
    private val repository: TodoRepository, // Keeping this for simple reads/deletes
    private val saveTodoUseCase: SaveTodoUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    private val _todos = mutableStateListOf<TodoItem>()
    val todos: List<TodoItem> get() = _todos

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            val fetchedTodos = repository.getAllTodos()
            _todos.clear()
            _todos.addAll(fetchedTodos)
        }
    }

    // 🚨 FIX 1: Context is removed. The Use Case handles the system-level details.
    fun saveNewHabit(draft: HabitDraft) {
        if (draft.name.isNotBlank()) {

            // Calculate the exact epoch time for the AlarmManager based on the UI strings
            val calculatedTime = calculateNextTriggerTime(draft.reminders)

            val newTodo = TodoItem(
                title = draft.name,
                status = Status.PENDING,
                type = draft.type,
                icon = draft.icon,
                colorArgb = draft.color.toArgb(),
                repeatGoal = draft.repeatGoal,
                category = draft.category,
                reminders = draft.reminders.joinToString(","),
                // Pass the generated Long down to the database!
                timeInMillis = calculatedTime
            )

            // Optimistic UI Update
            _todos.add(newTodo)

            viewModelScope.launch {
                saveTodoUseCase(newTodo)
                loadTodos()
            }
        }
    }

    // 🚨 FIX 2: We now actually persist the toggle to the database
    fun toggleTodo(todoId: String) {
        val index = _todos.indexOfFirst { it.id == todoId }

        if (index != -1) {
            // 1. Optimistic UI Update: Instantly show it as checked
            val todo = _todos[index]
            _todos[index] = todo.copy(status = Status.COMPLETED)

            // 2. Background Persistence & Rescheduling
            viewModelScope.launch {
                // This handles the streak logging, repetition math, and alarm updates
                completeTaskUseCase(todoId)

                // 3. Reload from DB. If it's a repeating daily habit, loadTodos()
                // will fetch the updated version where the time is bumped to tomorrow
                // and the status is reset back to PENDING.
                loadTodos()
            }
        }
    }

    fun deleteTodo(id: String) {
        // Optimistic UI Update
        _todos.removeAll { it.id == id }

        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class TodoViewModelFactory(
    private val repository: TodoRepository,
    private val saveTodoUseCase: SaveTodoUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(
                repository = repository,
                saveTodoUseCase = saveTodoUseCase,
                completeTaskUseCase = completeTaskUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}