package com.deliberate.codelab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        startDestination = startDestination
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

            // 1. Grab the context safely OUTSIDE the click event!
            val context = androidx.compose.ui.platform.LocalContext.current

            CreateHabitScreen(
                onBack = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0)
                    }
                },
                onSave = { habitDraft ->
                    // 2. Pass the pre-grabbed context in safely
                    viewModel.saveNewHabit(habitDraft)

                    navController.navigate(Routes.HOME) {
                        popUpTo(0)
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