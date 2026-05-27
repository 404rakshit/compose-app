package com.deliberate.codelab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deliberate.codelab.TodoViewModel
import com.deliberate.codelab.data.UserPreferences
import com.deliberate.codelab.ui.screens.CreateHabitScreen
import kotlinx.coroutines.launch
import com.deliberate.codelab.ui.screens.OnboardingScreen
import com.deliberate.codelab.ui.screens.TodoScreen

// We define our routes as simple strings
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"

    const val CREATE_HABIT = "create_habit"
    const val TASK_DETAIL = "task_detail/{taskId}" // We will use this later!
}

@Composable
fun AppNavigation(
    viewModel: TodoViewModel,
    startDestination: String, // Accept the dynamic start route
    userPreferences: UserPreferences // Accept the prefs object
) {
    // This controller remembers the back-stack (what screen you were on previously)
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // The NavHost swaps out the UI depending on the current route
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {

        // Route 1: The Main List
        composable(route = Routes.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = { selectedLang ->

                    // Put EVERYTHING inside the coroutine launch
                    coroutineScope.launch {

                        // 1. Wait for the save to physically finish on the disk
                        userPreferences.completeOnboarding(selectedLang)

                        // 2. NOW it is safe to navigate and destroy the screen
                        navController.navigate(Routes.CREATE_HABIT) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }

                }
            )
        }

        composable(route = Routes.HOME) {
            TodoScreen(
                viewModel = viewModel,
                onAddHabitClick = {
                    // Triggered by the Floating Action Button (+)
                    navController.navigate(Routes.CREATE_HABIT)
                }
            )
        }

        composable(route = Routes.CREATE_HABIT) {
            CreateHabitScreen(
                onBack = {
                    // If they hit the back arrow or phone's back button, go safely to Home.
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) // Clears back-history so Home is the root
                    }
                },
                onSave = { habitDraft ->
                    // TODO: Pass 'habitDraft' to your ViewModel to save to the database
                     viewModel.saveNewHabit(habitDraft)

                    // After saving, route them back to the Home Dashboard
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) // Clears back-history so Home is the root
                    }
                }
            )
        }

        // Route 2: The Details Screen (Placeholder for now)
        composable(route = Routes.TASK_DETAIL) { backStackEntry ->
            // Extracts the ID from the route string
            val taskId = backStackEntry.arguments?.getString("taskId")
            // TaskDetailScreen(taskId = taskId, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}