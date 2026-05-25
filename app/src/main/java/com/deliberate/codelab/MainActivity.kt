package com.deliberate.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.activity.enableEdgeToEdge
import com.deliberate.codelab.ui.navigation.AppNavigation
import com.deliberate.codelab.ui.theme.ProgressAppTheme
import android.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

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

