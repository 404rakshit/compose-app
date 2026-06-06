package com.deliberate.codelab

import android.content.Context
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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.deliberate.codelab.domain.usecase.CompleteTaskUseCase
import com.deliberate.codelab.domain.usecase.SaveTodoUseCase
import com.deliberate.codelab.util.AndroidAlarmScheduler
import com.deliberate.codelab.worker.DailyAlarmSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDailyWorkManager(applicationContext)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        val dbHelper = TodoDatabaseHelper(applicationContext)
        val repository = TodoRepository(dbHelper)

        val alarmScheduler = AndroidAlarmScheduler(applicationContext)

        val saveTodoUseCase = SaveTodoUseCase(repository, alarmScheduler)
        val completeTaskUseCase = CompleteTaskUseCase(repository, alarmScheduler)

        val viewModelFactory = TodoViewModelFactory(
            repository = repository,
            saveTodoUseCase = saveTodoUseCase,
            completeTaskUseCase = completeTaskUseCase
        )

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

private fun setupDailyWorkManager(context: Context) {
    // Request the worker to run approximately every 24 hours
    val syncRequest = PeriodicWorkRequestBuilder<DailyAlarmSyncWorker>(24, TimeUnit.HOURS)
        // Optional: Make it run only when the battery isn't dying
        // .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
        .build()

    // Enqueue Unique ensures we don't accidentally schedule multiple overlapping jobs
    // if the user opens the app 10 times a day.
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "DAILY_ALARM_SYNC",
        ExistingPeriodicWorkPolicy.KEEP, // Keep the existing schedule if it's already running
        syncRequest
    )
}