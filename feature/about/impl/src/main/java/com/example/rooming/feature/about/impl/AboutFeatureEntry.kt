package com.example.rooming.feature.about.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.rooming.core.common.CrashReporter
import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.core.navigation.TopLevelDestination
import com.example.rooming.core.ui.R
import com.example.rooming.feature.about.api.BuildVariantConfig
import com.example.rooming.feature.about.api.AboutFeatureApi
import com.example.rooming.feature.about.api.MapConfig
import com.example.rooming.feature.about.api.RemoteConfigService
import com.example.rooming.feature.about.api.UserProfileService
import com.example.rooming.feature.auth.api.AuthFeatureApi
import com.example.rooming.feature.auth.api.AuthService
import javax.inject.Inject

class AboutFeatureEntry @Inject constructor(
    private val mapConfig: MapConfig,
    private val buildVariantConfig: BuildVariantConfig,
    private val remoteConfigService: RemoteConfigService,
    private val userProfileService: UserProfileService,
    private val crashReporter: CrashReporter,
    private val authService: AuthService,
) : FeatureEntry {
    override val topLevelDestination = TopLevelDestination(
        route = AboutFeatureApi.route,
        labelRes = R.string.nav_about,
        icon = Icons.Outlined.Business,
        order = 3,
    )

    override fun NavGraphBuilder.register(navController: NavHostController) {
        composable(route = AboutFeatureApi.route) {
            AboutRoute(
                mapConfig = mapConfig,
                buildVariantConfig = buildVariantConfig,
                remoteConfigService = remoteConfigService,
                userProfileService = userProfileService,
                crashReporter = crashReporter,
                onLogoutClick = {
                    authService.logout()
                    userProfileService.clear()
                    navController.navigate(AuthFeatureApi.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
