package com.example.rooming.feature.favorites.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.navigation.TopLevelDestination
import com.example.rooming.core.ui.R
import com.example.rooming.feature.favorites.api.FavoritesFeatureApi
import com.example.rooming.feature.rooms.api.RoomsFeatureApi
import javax.inject.Inject

class FavoritesFeatureEntry @Inject constructor() : FeatureEntry {
    override val topLevelDestination = TopLevelDestination(
        route = FavoritesFeatureApi.route,
        labelRes = R.string.nav_favorites,
        icon = Icons.Outlined.FavoriteBorder,
        order = 1,
    )

    override fun NavGraphBuilder.register(navController: NavHostController) {
        composable(route = FavoritesFeatureApi.route) {
            FavoritesRoute(
                onRoomClick = { roomId ->
                    navController.navigate(RoomsFeatureApi.detailsRoute(roomId))
                },
            )
        }
    }
}
