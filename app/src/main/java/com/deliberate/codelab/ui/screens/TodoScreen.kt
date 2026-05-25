package com.deliberate.codelab.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deliberate.codelab.TodoViewModel
import com.deliberate.codelab.ui.components.CreateTaskDialog
import com.deliberate.codelab.ui.components.TodoRow

@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    // State to control when the creation dialog appears
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // The Floating Action Button Setup
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Task"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "My Tasks",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = viewModel.todos, key = { it.id }) { todo ->
                    // For now, let's just display the title and priority
                    TodoRow(
                        todo = todo,
                        onToggle = { viewModel.toggleTodo(todo.id) },
                        onDelete = { viewModel.deleteTodo(todo.id) }
                    )
                }
            }
        }

        // Show the dialog if the FAB was clicked
        if (showAddDialog) {
            CreateTaskDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, desc, repetition, timeInMillis ->
                    viewModel.addTodo(
                        title = title,
                        description = desc,
                        repetition = repetition, // Make sure your viewModel.addTodo accepts timeInMillis too!
                        timeInMillis = timeInMillis // <- Add this if you updated your ViewModel to accept it
                    )
                    showAddDialog = false
                }
            )
        }
    }
}