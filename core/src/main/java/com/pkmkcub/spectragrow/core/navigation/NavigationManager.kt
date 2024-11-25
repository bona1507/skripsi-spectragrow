package com.pkmkcub.spectragrow.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

class NavigationManager(private val destinations: List<NavigationDestination?>) {
    fun registerAll(navController: NavHostController, navGraphBuilder: NavGraphBuilder) {
        destinations.forEach { destination ->
            destination?.registerGraph(navController, navGraphBuilder)
        }
    }
}
