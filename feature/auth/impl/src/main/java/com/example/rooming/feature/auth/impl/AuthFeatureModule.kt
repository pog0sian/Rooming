package com.example.rooming.feature.auth.impl

import com.example.rooming.core.navigation.FeatureEntry
import com.example.rooming.feature.auth.api.AuthService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthFeatureModule {
    @Binds
    @IntoSet
    abstract fun bindAuthFeatureEntry(entry: AuthFeatureEntry): FeatureEntry

    @Binds
    @Singleton
    abstract fun bindAuthService(service: DefaultAuthService): AuthService
}
