package com.example.rooming.core.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

data class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val order: Int,
)

interface FeatureEntry {
    val topLevelDestination: TopLevelDestination?

    fun NavGraphBuilder.register(navController: NavHostController)
}

fun NavDestination?.isTopLevelRoute(route: String): Boolean =
    this?.hierarchy?.any { destination -> destination.route == route } == true
