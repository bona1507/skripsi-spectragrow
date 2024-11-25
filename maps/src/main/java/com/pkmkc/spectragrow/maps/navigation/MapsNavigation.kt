package com.pkmkc.spectragrow.maps.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pkmkc.spectragrow.maps.ui.DetailsItemScreen
import com.pkmkc.spectragrow.maps.ui.MapsScreen
import com.pkmkcub.spectragrow.core.navigation.NavigationDestination

class MapsNavigation : NavigationDestination {

    override val route: String = "maps"

    override fun registerGraph(navController: androidx.navigation.NavHostController, navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.composable("maps") {
            MapsScreen(navController)
        }
        navGraphBuilder.composable("detailitem") {
            DetailsItemScreen()
        }
    }
}

