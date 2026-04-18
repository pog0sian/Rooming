package com.example.rooming.feature.rooms.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.navigation.TopLevelDestination
import com.example.rooming.feature.rooms.api.RoomsFeatureApi
import javax.inject.Inject

class RoomsFeatureEntry @Inject constructor() : FeatureEntry {
    override val topLevelDestination = TopLevelDestination(
        route = RoomsFeatureApi.route,
        label = "Rooms",
        icon = Icons.Outlined.Home,
        order = 0,
    )

    override fun NavGraphBuilder.register(navController: NavHostController) {
        composable(route = RoomsFeatureApi.route) {
            RoomsRoute(
                onRoomClick = { roomId ->
                    navController.navigate(RoomsFeatureApi.detailsRoute(roomId))
                },
            )
        }

        composable(
            route = RoomsFeatureApi.detailsRoutePattern,
            arguments = listOf(
                navArgument(RoomsFeatureApi.roomIdArg) {
                    type = NavType.StringType
                },
            ),
        ) {
            RoomDetailsRoute(onBackClick = navController::popBackStack)
        }
    }
}
