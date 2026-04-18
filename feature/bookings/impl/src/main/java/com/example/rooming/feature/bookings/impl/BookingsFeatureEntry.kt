package com.example.rooming.feature.bookings.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.navigation.TopLevelDestination
import com.example.rooming.core.ui.R
import com.example.rooming.feature.bookings.api.BookingsFeatureApi
import javax.inject.Inject

class BookingsFeatureEntry @Inject constructor() : FeatureEntry {
    override val topLevelDestination = TopLevelDestination(
        route = BookingsFeatureApi.route,
        labelRes = R.string.nav_bookings,
        icon = Icons.AutoMirrored.Outlined.EventNote,
        order = 2,
    )

    override fun NavGraphBuilder.register(navController: NavHostController) {
        composable(route = BookingsFeatureApi.route) {
            BookingsRoute()
        }
    }
}
