package com.pkmkcub.spectragrow

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.pkmkcub.spectragrow.core.navigation.NavigationDestination
import com.pkmkcub.spectragrow.core.navigation.NavigationManager
import com.pkmkcub.spectragrow.view.ui.auth.AuthViewModel
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val navigationManager by lazy {
        val storyNavigation = try {
            Class.forName("com.pkmkc.spectragrow.story.navigation.StoryNavigation")
                .getDeclaredConstructor()
                .newInstance() as NavigationDestination
        } catch (e: Exception) {
            null
        }
        val mapsNavigation = try {
            Class.forName("com.pkmkc.spectragrow.maps.navigation.MapsNavigation")
                .getDeclaredConstructor()
                .newInstance() as NavigationDestination
        } catch (e: Exception) {
            null
        }

        listOfNotNull(
            AppNavigation(authViewModel),
            storyNavigation,
            mapsNavigation
        ).let {
            NavigationManager(destinations = it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        val currentUser = authViewModel.getCurrentUser()

        setContent {
            val navController = rememberNavController()
            SetupBackPressedDispatcher(navController)
            SpectraNavHost(
                navController = navController,
                startDestination = if (currentUser != null) "home" else "onboarding",
                navigationManager = navigationManager
            )
        }
    }

    @Composable
    private fun SetupBackPressedDispatcher(navController: NavHostController) {
        onBackPressedDispatcher.addCallback(this) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            when (currentRoute) {
                "home" -> exitProcess(0)
                "liststory" -> navController.popBackStack("home", false)
                "addstory" -> navController.popBackStack("liststory", false)
                else -> finish()
            }
        }
    }
}


@Composable
fun SpectraNavHost(
    navController: NavHostController,
    startDestination: String,
    navigationManager: NavigationManager
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        navigationManager.registerAll(navController, this)
    }
}
