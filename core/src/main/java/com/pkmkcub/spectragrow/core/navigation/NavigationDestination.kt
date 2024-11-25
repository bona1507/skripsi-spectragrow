package com.pkmkcub.spectragrow.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface NavigationDestination {
    val route: String
    fun registerGraph(navController: NavHostController, navGraphBuilder: NavGraphBuilder)
}