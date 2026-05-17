package com.example.rooming.feature.auth.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.navigation.TopLevelDestination
import com.example.rooming.feature.about.api.UserProfileService
import com.example.rooming.feature.auth.api.AuthConfig
import com.example.rooming.feature.auth.api.AuthFeatureApi
import javax.inject.Inject

class AuthFeatureEntry @Inject constructor(
    private val authConfig: AuthConfig,
    private val userProfileService: UserProfileService,
) : FeatureEntry {
    override val topLevelDestination: TopLevelDestination? = null

    override fun NavGraphBuilder.register(navController: NavHostController) {
        composable(route = AuthFeatureApi.route) {
            LoginRoute(
                authConfig = authConfig,
                onLoggedIn = {
                    userProfileService.refresh()
                    navController.popBackStack()
                },
            )
        }
    }

    override fun NavGraphBuilder.register(
        navController: NavHostController,
        topLevelDestinations: List<TopLevelDestination>,
    ) {
        val targetRoute = topLevelDestinations.firstOrNull()?.route.orEmpty()

        composable(route = AuthFeatureApi.route) {
            LoginRoute(
                authConfig = authConfig,
                onLoggedIn = {
                    userProfileService.refresh()
                    if (targetRoute.isNotBlank()) {
                        navController.navigate(targetRoute) {
                            popUpTo(AuthFeatureApi.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    }
}
