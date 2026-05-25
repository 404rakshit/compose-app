package com.deliberate.codelab

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deliberate.codelab.domain.model.Priority
import com.deliberate.codelab.domain.model.Repetition
import com.deliberate.codelab.domain.model.Status
import com.deliberate.codelab.domain.model.TodoItem
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _todos = mutableStateListOf<TodoItem>()
    val todos: List<TodoItem> get() = _todos

    init {
        loadTodos()
    }

    private fun loadTodos() {
        // Launches a coroutine tied to this ViewModel's lifecycle
        viewModelScope.launch {
            // Suspends here until the DB returns the data, without freezing the UI!
            val fetchedTodos = repository.getAllTodos()
            _todos.clear()
            _todos.addAll(fetchedTodos)
        }
    }

    fun addTodo(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        repetition: Repetition = Repetition.NONE,
        timeInMillis: Long? = null
    ) {
        if (title.isNotBlank()) {
            // Create the complex item using our new Enums
            val newTodo = TodoItem(
                title = title,
                description = description,
                priority = priority,
                repetition = repetition,
                status = Status.PENDING
            )

            // Instantly update the UI
            _todos.add(newTodo)

            // Fire and forget the database save in the background
            viewModelScope.launch {
                repository.insertTodo(newTodo)
            }
        }
    }

    fun toggleTodo(id: String) {
        val index = _todos.indexOfFirst { it.id == id }
        if (index != -1) {
            val currentTask = _todos[index]
            // If we are moving from Pending -> Completed, log it!
            val isNowCompleted = currentTask.status != Status.COMPLETED
            val newStatus = if (isNowCompleted) Status.COMPLETED else Status.PENDING

            // Optimistic UI update
            _todos[index] = currentTask.copy(status = newStatus)

            viewModelScope.launch {
                // Update the main task row
                repository.updateTodoStatus(id, newStatus)

                // Write to the history log to build the streak
                if (isNowCompleted) {
                    repository.logTaskCompletion(id)

                    // You can now fetch the updated streak to display in the UI!
                    // val newStreak = repository.calculateCurrentStreak(id)
                }
            }
        }
    }

    fun deleteTodo(id: String) {
        // Optimistic UI update
        _todos.removeAll { it.id == id }

        // Background sync
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