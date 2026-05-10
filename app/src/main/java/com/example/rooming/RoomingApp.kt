package com.example.rooming

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.navigation.isTopLevelRoute
import com.example.rooming.feature.auth.api.AuthFeatureApi
import com.example.rooming.feature.rooms.api.RoomsFeatureApi

@Composable
fun RoomingApp(
    featureEntries: List<FeatureEntry>,
    hasAuthorizedSession: Boolean,
    notificationRoomId: String? = null,
    onNotificationHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val topLevelDestinations = featureEntries
        .mapNotNull(FeatureEntry::topLevelDestination)
        .sortedBy { destination -> destination.order }
    val firstTopLevelRoute = topLevelDestinations.firstOrNull()?.route.orEmpty()
    val startDestination = if (hasAuthorizedSession) {
        firstTopLevelRoute
    } else {
        AuthFeatureApi.route
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute != null &&
        currentRoute != AuthFeatureApi.route &&
        topLevelDestinations.isNotEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    topLevelDestinations.forEach { destination ->
                        val label = stringResource(destination.labelRes)
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
                                    contentDescription = label,
                                )
                            },
                            label = { Text(label) },
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
                    register(navController, topLevelDestinations)
                }
            }
        }
    }

    LaunchedEffect(notificationRoomId, hasAuthorizedSession) {
        val roomId = notificationRoomId?.takeIf(String::isNotBlank) ?: return@LaunchedEffect
        if (hasAuthorizedSession) {
            navController.navigate(RoomsFeatureApi.detailsRoute(roomId)) {
                launchSingleTop = true
            }
            onNotificationHandled()
        }
    }
}
