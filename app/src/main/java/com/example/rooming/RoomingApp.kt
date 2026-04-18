package com.example.rooming

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.navigation.isTopLevelRoute

@Composable
fun RoomingApp(featureEntries: List<FeatureEntry>) {
    val navController = rememberNavController()
    val topLevelDestinations = featureEntries
        .mapNotNull(FeatureEntry::topLevelDestination)
        .sortedBy { destination -> destination.order }
    val startDestination = topLevelDestinations.firstOrNull()?.route.orEmpty()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            if (topLevelDestinations.isNotEmpty()) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = navBackStackEntry?.destination.isTopLevelRoute(destination.route),
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            featureEntries.forEach { entry ->
                with(entry) {
                    register(navController)
                }
            }
        }
    }
}
