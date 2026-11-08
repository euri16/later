package dev.euryperez.later.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.euryperez.later.ui.detail.TaskDetailScreen
import dev.euryperez.later.ui.edit.EditTaskScreen
import dev.euryperez.later.ui.list.TaskListScreen
import dev.euryperez.later.ui.stats.StatsScreen

private object Routes {
    const val TASKS = "tasks"
    const val STATS = "stats"
    const val DETAIL = "task/{taskId}"
    const val EDIT = "edit?taskId={taskId}"

    fun detail(id: Long) = "task/$id"
    fun edit(id: Long? = null) = if (id == null) "edit" else "edit?taskId=$id"
}

@Composable
fun LaterNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route == Routes.TASKS || route == Routes.STATS

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    listOf(
                        Triple(Routes.TASKS, "Tasks", Icons.AutoMirrored.Filled.List),
                        Triple(Routes.STATS, "Stats", Icons.Default.Star),
                    ).forEach { (destination, label, icon) ->
                        NavigationBarItem(
                            selected = route == destination,
                            onClick = {
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TASKS,
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .consumeWindowInsets(padding),
        ) {
            composable(Routes.TASKS) {
                TaskListScreen(
                    onTaskClick = { navController.navigate(Routes.detail(it)) },
                    onAddTask = { navController.navigate(Routes.edit()) },
                )
            }
            composable(Routes.STATS) { StatsScreen() }
            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
            ) {
                TaskDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.edit(it)) },
                )
            }
            composable(
                Routes.EDIT,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                ),
            ) {
                EditTaskScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
