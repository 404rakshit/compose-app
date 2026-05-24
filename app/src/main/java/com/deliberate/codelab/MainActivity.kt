package com.deliberate.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

// ==========================================
// 1. THE MODEL (Domain Entity)
// ==========================================
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    var isCompleted: Boolean = false
)

// ==========================================
// 2. THE VIEWMODEL (State & Logic)
// ==========================================
// ViewModels survive screen rotations. Never put UI Contexts in here.

// ==========================================
// 3. THE UI (Jetpack Compose)
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manual Dependency Injection
        val dbHelper = TodoDatabaseHelper(applicationContext)
        val repository = TodoRepository(dbHelper)
        val viewModelFactory = TodoViewModelFactory(repository)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Inject the ViewModel using our custom factory
                    TodoScreen(
                        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = viewModelFactory
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    // remember { mutableStateOf() } is exactly like React's useState.
    // We use it here for temporary UI state (the text input field).
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "My Tasks",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // The Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("New task...") },
                modifier = Modifier.weight(1f) // Takes up remaining horizontal space
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                viewModel.addTodo(inputText)
                inputText = "" // Clear the input after adding
            }) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LazyColumn is Android's highly optimized equivalent to mapping over an array.
        // It only renders items currently visible on the screen.
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = viewModel.todos,
                key = { it.id } // Providing a key prevents unnecessary re-renders
            ) { todo ->
                TodoRow(
                    todo = todo,
                    onToggle = { viewModel.toggleTodo(todo.id) },
                    onDelete = { viewModel.deleteTodo(todo.id) }
                )
            }
        }
    }
}

@Composable
fun TodoRow(
    todo: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = todo.title,
                modifier = Modifier.weight(1f),
                style = if (todo.isCompleted)
                    MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                else
                    MaterialTheme.typography.bodyLarge
            )

            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}