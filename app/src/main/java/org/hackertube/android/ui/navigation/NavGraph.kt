package org.hackertube.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.repository.VideoRepository
import org.hackertube.android.ui.screens.CategoryVideosScreen
import org.hackertube.android.ui.screens.ExploreScreen
import org.hackertube.android.ui.screens.HomeScreen
import org.hackertube.android.ui.screens.LibraryScreen
import org.hackertube.android.ui.screens.LikedScreen
import org.hackertube.android.ui.screens.SearchScreen
import org.hackertube.android.ui.screens.SettingsScreen
import org.hackertube.android.ui.screens.VideoDetailScreen

sealed class Dest(val route: String) {
    data object Home : Dest("home")
    data object Explore : Dest("explore")
    data object Liked : Dest("liked")
    data object Library : Dest("library")
    data object Settings : Dest("settings")
    data object Search : Dest("search")
    data object Video : Dest("video/{vid}") {
        fun create(vid: String) = "video/$vid"
    }
    data object Category : Dest("category/{id}/{name}") {
        fun create(id: String, name: String) =
            "category/$id/${android.net.Uri.encode(name)}"
    }
}

data class BottomItem(
    val dest: Dest,
    val label: String,
    val selected: ImageVector,
    val unselected: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Dest.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomItem(Dest.Explore, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    BottomItem(Dest.Liked, "Liked", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomItem(Dest.Library, "Library", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    BottomItem(Dest.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun HackerTubeNavHost(
    videoRepository: VideoRepository,
    userPreferences: UserPreferences
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        Dest.Home.route, Dest.Explore.route, Dest.Liked.route,
        Dest.Library.route, Dest.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.dest.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selected else item.unselected,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Dest.Home.route) {
                HomeScreen(
                    repository = videoRepository,
                    userPreferences = userPreferences,
                    onVideoClick = { navController.navigate(Dest.Video.create(it)) },
                    onSearchClick = { navController.navigate(Dest.Search.route) }
                )
            }
            composable(Dest.Explore.route) {
                ExploreScreen(
                    repository = videoRepository,
                    onCategoryClick = { id, name ->
                        navController.navigate(Dest.Category.create(id, name))
                    },
                    onSearchClick = { navController.navigate(Dest.Search.route) }
                )
            }
            composable(Dest.Liked.route) {
                LikedScreen(
                    repository = videoRepository,
                    userPreferences = userPreferences,
                    onVideoClick = { navController.navigate(Dest.Video.create(it)) }
                )
            }
            composable(Dest.Library.route) {
                LibraryScreen(
                    repository = videoRepository,
                    userPreferences = userPreferences,
                    onVideoClick = { navController.navigate(Dest.Video.create(it)) }
                )
            }
            composable(Dest.Settings.route) {
                SettingsScreen(userPreferences = userPreferences)
            }
            composable(Dest.Search.route) {
                SearchScreen(
                    repository = videoRepository,
                    userPreferences = userPreferences,
                    onVideoClick = { navController.navigate(Dest.Video.create(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Dest.Video.route,
                arguments = listOf(navArgument("vid") { type = NavType.StringType })
            ) { entry ->
                val vid = entry.arguments?.getString("vid") ?: return@composable
                VideoDetailScreen(
                    vid = vid,
                    repository = videoRepository,
                    userPreferences = userPreferences,
                    onBack = { navController.popBackStack() },
                    onRelatedClick = { navController.navigate(Dest.Video.create(it)) }
                )
            }
            composable(
                route = Dest.Category.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                val name = entry.arguments?.getString("name") ?: id
                CategoryVideosScreen(
                    categoryId = id,
                    categoryName = name,
                    repository = videoRepository,
                    userPreferences = userPreferences,
                    onVideoClick = { navController.navigate(Dest.Video.create(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
