package com.deliberate.codelab

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliberate.codelab.ui.screens.HabitDraft // Make sure this matches where you put HabitDraft!
import com.deliberate.quickalarm.domain.model.Status
import com.deliberate.quickalarm.domain.model.TodoItem
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

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

    // --- REPLACED addTodo WITH THIS ---
    fun saveNewHabit(draft: HabitDraft) {
        if (draft.name.isNotBlank()) {
            val newTodo = TodoItem(
                // We leave 'id' blank so the repository can generate a UUID
                title = draft.name,
                status = Status.PENDING,
                type = draft.type,
                icon = draft.icon,
                colorArgb = draft.color.toArgb(), // Converts Compose Color to Int
                repeatGoal = draft.repeatGoal,
                category = draft.category,
                reminders = draft.reminders.joinToString(",") // Converts List to String
            )

            // Optimistic UI Update so the user feels no lag
            _todos.add(newTodo)

            // Fire and forget the DB save
            viewModelScope.launch {
                repository.insert(newTodo) // Using the updated insert method
                // Reload from DB to ensure we grab the auto-generated UUID for future edits/deletes
                loadTodos()
            }
        }
    }

    fun toggleTodo(id: String) {
        val index = _todos.indexOfFirst { it.id == id }
        if (index != -1) {
            val currentTask = _todos[index]
            val isNowCompleted = currentTask.status != Status.COMPLETED
            val newStatus = if (isNowCompleted) Status.COMPLETED else Status.PENDING

            // Optimistic UI update
            _todos[index] = currentTask.copy(status = newStatus)

            viewModelScope.launch {
                repository.updateTodoStatus(id, newStatus)

                if (isNowCompleted) {
                    repository.logTaskCompletion(id)
                }
            }
        }
    }

    fun deleteTodo(id: String) {
        _todos.removeAll { it.id == id }

        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class TodoViewModelFactory(private val repository: TodoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodoViewModel(repository) as T
    }
}