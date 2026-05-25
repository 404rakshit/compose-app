package com.deliberate.codelab.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deliberate.codelab.TodoViewModel
import com.deliberate.codelab.ui.screens.TodoScreen

// We define our routes as simple strings
object Routes {
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
        startDestination = Routes.HOME
    ) {

        // Route 1: The Main List
        composable(route = Routes.HOME) {
            TodoScreen(
                viewModel = viewModel,
                // Later, we will pass a lambda here to tell TodoScreen how to navigate to Details
                // onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") }
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