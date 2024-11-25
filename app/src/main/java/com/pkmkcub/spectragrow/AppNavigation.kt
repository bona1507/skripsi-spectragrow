package com.pkmkcub.spectragrow

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.pkmkcub.spectragrow.core.navigation.NavigationDestination
import com.pkmkcub.spectragrow.view.ui.auth.AuthViewModel
import com.pkmkcub.spectragrow.view.ui.auth.LoginScreen
import com.pkmkcub.spectragrow.view.ui.auth.RegisterScreen
import com.pkmkcub.spectragrow.view.ui.main.HomeScreen
import com.pkmkcub.spectragrow.view.ui.onboarding.OnboardingScreen

class AppNavigation(
    private val authViewModel: AuthViewModel
) : NavigationDestination {

    override val route: String = "app"

    override fun registerGraph(navController: NavHostController, navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.composable("login") {
            LoginScreen(navController, authViewModel)
        }
        navGraphBuilder.composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        navGraphBuilder.composable("home") {
            HomeScreen(navController)
        }
        navGraphBuilder.composable("onboarding") {
            OnboardingScreen(navController)
        }
    }
}

