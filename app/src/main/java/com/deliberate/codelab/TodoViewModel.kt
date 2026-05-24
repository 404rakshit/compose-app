package com.deliberate.codelab

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _todos = mutableStateListOf<TodoItem>()
    val todos: List<TodoItem> get() = _todos

    init {
        loadTodos()
    }

    private fun loadTodos() {
        _todos.clear()
        _todos.addAll(repository.getAllTodos())
    }

    fun addTodo(title: String) {
        if (title.isNotBlank()) {
            val newTodo = TodoItem(title = title)
            repository.insertTodo(newTodo) // Save to DB
            _todos.add(newTodo)            // Update UI state
        }
    }

    fun toggleTodo(id: String) {
        val index = _todos.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedStatus = !_todos[index].isCompleted
            repository.updateTodoStatus(id, updatedStatus) // Save to DB

            // Update UI state immutably
            _todos[index] = _todos[index].copy(isCompleted = updatedStatus)
        }
    }

    fun deleteTodo(id: String) {
        repository.deleteTodo(id) // Delete from DB
        _todos.removeAll { it.id == id } // Update UI state
    }
}

// The Factory ensures we can inject the Repository into the ViewModel
@Suppress("UNCHECKED_CAST")
class TodoViewModelFactory(private val repository: TodoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodoViewModel(repository) as T
    }
}