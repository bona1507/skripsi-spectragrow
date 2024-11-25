package com.pkmkc.spectragrow.story.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pkmkc.spectragrow.story.ui.AddStoryScreen
import com.pkmkc.spectragrow.story.ui.DetailStoryScreen
import com.pkmkc.spectragrow.story.ui.ListStoryScreen
import com.pkmkcub.spectragrow.core.navigation.NavigationDestination

class StoryNavigation : NavigationDestination {

    override val route: String = "story"

    override fun registerGraph(navController: androidx.navigation.NavHostController, navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.composable("liststory") {
            ListStoryScreen(
                onAddStory = { navController.navigate("addstory") },
                onStoryClick = { story ->
                    navController.navigate("detailStory/${story.title}")
                }
            )
        }
        navGraphBuilder.composable("addstory") {
            AddStoryScreen(
                onNavigateToListStory = { navController.navigate("liststory") }
            )
        }
        navGraphBuilder.composable("detailStory/{title}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            DetailStoryScreen(title = title)
        }
    }
}

