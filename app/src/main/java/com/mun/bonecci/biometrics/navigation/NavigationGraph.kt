package com.mun.bonecci.biometrics.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mun.bonecci.biometrics.LoginScreen
import com.mun.bonecci.biometrics.ResultScreen


/**
 * Composable function that defines the navigation graph using Jetpack Compose Navigation.
 *
 * @param navController The navigation controller that manages navigation within the graph.
 */
@Composable
fun NavigationGraph(navController: NavHostController) {
    // Create a navigation graph using NavHost
    NavHost(navController, startDestination = NavigationItem.LoginScreen.route) {
        // Define the composable for Login Screen
        composable(NavigationItem.LoginScreen.route) {
            // Call the appropriate Login Screen content here
            LoginScreen(navController) {}
        }

        // Define the composable for ScreenB with a dynamic parameter (email, password)
        composable(
            "${NavigationItem.ResultScreen.route}/{email}/{password}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Retrieve the email parameter from the arguments
            val email = backStackEntry.arguments?.getString("email", "")
            // Retrieve the password parameter from the arguments
            val password = backStackEntry.arguments?.getString("password", "")

            // Call the appropriate ScreenB content here, passing the userName and userAge
            ResultScreen(email, password)
        }
    }
}