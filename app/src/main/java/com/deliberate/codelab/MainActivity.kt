package com.deliberate.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.activity.enableEdgeToEdge
import com.deliberate.codelab.ui.navigation.AppNavigation
import com.deliberate.codelab.ui.theme.ProgressAppTheme
import com.deliberate.codelab.ui.screens.TodoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbHelper = TodoDatabaseHelper(applicationContext)
        val repository = TodoRepository(dbHelper)
        val viewModelFactory = TodoViewModelFactory(repository)

        setContent {
            ProgressAppTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
                    )
                }
            }
        }
    }
}

