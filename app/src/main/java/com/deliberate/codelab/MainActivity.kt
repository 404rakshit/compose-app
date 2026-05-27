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
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deliberate.codelab.data.UserPreferences
import com.deliberate.codelab.ui.navigation.Routes
import androidx.compose.runtime.getValue

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

        val userPreferences = UserPreferences(applicationContext)

        setContent {
            ProgressAppTheme() {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    // 1. Start with null
                    val hasSeenOnboarding by userPreferences.hasSeenOnboardingFlow.collectAsState(initial = null)

                    android.util.Log.d("DataStoreDebug", "4. MainActivity UI State is currently: $hasSeenOnboarding")

                    if (hasSeenOnboarding == null) {
                        android.util.Log.d("DataStoreDebug", "5. State is null, holding blank screen...")
                        return@Surface
                    }

                    val startRoute = if (hasSeenOnboarding == true) Routes.HOME else Routes.ONBOARDING
                    android.util.Log.d("DataStoreDebug", "6. Routing user to: $startRoute")

                    AppNavigation(
                        viewModel = viewModel(factory = viewModelFactory),
                        startDestination = startRoute, // Pass it to the router
                        userPreferences = userPreferences // Pass the prefs so the Onboarding screen can save the choice
                    )
                }
            }
        }
    }
}

