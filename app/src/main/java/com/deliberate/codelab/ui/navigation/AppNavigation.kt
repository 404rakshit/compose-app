package com.deliberate.codelab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deliberate.codelab.TodoViewModel
import com.deliberate.codelab.ui.screens.OnboardingScreen
import com.deliberate.codelab.ui.screens.TodoScreen

// We define our routes as simple strings
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TASK_DETAIL = "task_detail/{taskId}" // We will use this later!
}

@Composable
fun AppNavigation(viewModel: TodoViewModel) {
    // This controller remembers the back-stack (what screen you were on previously)
    val navController = rememberNavController()

    // The NavHost swaps out the UI depending on the current route
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {

        // Route 1: The Main List
        composable(route = Routes.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = { selectedLang ->
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Routes.HOME) {
            TodoScreen(viewModel = viewModel)
        }

        // Route 2: The Details Screen (Placeholder for now)
        composable(route = Routes.TASK_DETAIL) { backStackEntry ->
            // Extracts the ID from the route string
            val taskId = backStackEntry.arguments?.getString("taskId")
            // TaskDetailScreen(taskId = taskId, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}