package com.pkmkcub.spectragrow

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.pkmkcub.spectragrow.view.ui.auth.AuthViewModel
import com.pkmkcub.spectragrow.view.ui.auth.LoginScreen
import com.pkmkcub.spectragrow.view.ui.auth.RegisterScreen
import com.pkmkcub.spectragrow.view.ui.detailsitem.DetailsItemScreen
import com.pkmkcub.spectragrow.view.ui.main.HomeScreen
import com.pkmkcub.spectragrow.view.ui.maps.MapsScreen
import com.pkmkcub.spectragrow.view.ui.maps.MapsViewModel
import com.pkmkcub.spectragrow.view.ui.maps.MapsViewModelFactory
import com.pkmkcub.spectragrow.view.ui.onboarding.OnboardingScreen
import com.pkmkcub.spectragrow.view.ui.userstory.AddStoryScreen
import com.pkmkcub.spectragrow.view.ui.userstory.DetailStoryScreen
import com.pkmkcub.spectragrow.view.ui.userstory.ListStoryScreen

class MainActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private val mapsViewModel: MapsViewModel by viewModels {
        MapsViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        val currentUser = viewModel.getCurrentUser()
        setContent {
            SpectraNavHost(navController = rememberNavController(), startDestination = if (currentUser != null) "home" else "onboarding")
        }
    }

    @Composable
    fun SpectraNavHost(navController: NavHostController, startDestination: String) {
        val backPressedCallback = rememberUpdatedState {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            when (currentRoute) {
                "liststory" -> {
                    navController.popBackStack("home", false)
                }
                "addstory" -> {
                    navController.popBackStack("liststory", false)
                }
                "detailStory/{title}" -> {
                    navController.popBackStack("liststory", false)
                }
                else -> {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            backPressedCallback.value.invoke()
        }

        NavHost(navController = navController, startDestination = startDestination) {
            composable("onboarding") {
                OnboardingScreen(navController)
            }
            composable("login") {
                LoginScreen(navController, viewModel)
            }
            composable("register") {
                RegisterScreen(navController, viewModel)
            }
            composable("home") {
                HomeScreen(navController)
            }
            composable("maps") {
                MapsScreen(mapsViewModel, navController)
            }
            composable("liststory") {
                ListStoryScreen(
                    onAddStory = { navController.navigate("addstory") },
                    onStoryClick = { story ->
                        navController.navigate("detailStory/${story.title}")
                    }
                )
            }
            composable("addstory") {
                AddStoryScreen(
                    onNavigateToListStory = { navController.navigate("liststory") }
                )
            }
            composable("detailStory/{title}") { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                DetailStoryScreen(title = title)
            }
            composable("detailitem") {
                DetailsItemScreen()
            }
        }
    }
}
