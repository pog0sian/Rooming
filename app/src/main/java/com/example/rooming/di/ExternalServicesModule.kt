package com.example.rooming.di

import com.example.rooming.BuildConfig
import com.example.rooming.core.analytics.AnalyticsService
import com.example.rooming.core.analytics.AppMetricaAnalyticsService
import com.example.rooming.core.common.CrashReporter
import com.example.rooming.crash.CompositeCrashReporter
import com.example.rooming.feature.about.api.BuildVariantConfig
import com.example.rooming.feature.about.api.RemoteConfigService
import com.example.rooming.feature.about.api.MapConfig
import com.example.rooming.feature.about.api.UserProfileService
import com.example.rooming.firebase.FirebaseRemoteConfigService
import com.example.rooming.firebase.FirebaseUserProfileService
import com.example.rooming.feature.auth.api.AuthConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsService(service: AppMetricaAnalyticsService): AnalyticsService

    @Binds
    @Singleton
    abstract fun bindCrashReporter(reporter: CompositeCrashReporter): CrashReporter

    @Binds
    @Singleton
    abstract fun bindRemoteConfigService(service: FirebaseRemoteConfigService): RemoteConfigService

    @Binds
    @Singleton
    abstract fun bindUserProfileService(service: FirebaseUserProfileService): UserProfileService
}

@Module
@InstallIn(SingletonComponent::class)
object ExternalConfigModule {
    @Provides
    fun provideAuthConfig(): AuthConfig = AuthConfig(
        yandexClientId = BuildConfig.YANDEX_CLIENT_ID,
        vkClientId = BuildConfig.VK_CLIENT_ID,
        vkClientSecret = BuildConfig.VK_CLIENT_SECRET,
    )

    @Provides
    fun provideMapConfig(): MapConfig = MapConfig(
        yandexMapKitApiKey = BuildConfig.YANDEX_MAPKIT_API_KEY,
    )

    @Provides
    fun provideBuildVariantConfig(): BuildVariantConfig = BuildVariantConfig(
        environmentName = BuildConfig.ROOMING_ENV,
        labToolsEnabled = BuildConfig.LAB_TOOLS_ENABLED,
    )
}
