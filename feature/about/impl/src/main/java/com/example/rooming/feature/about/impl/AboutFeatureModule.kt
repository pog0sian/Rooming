package com.example.rooming.feature.about.impl

import com.example.rooming.core.navigation.FeatureEntry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AboutFeatureModule {
    @Binds
    @IntoSet
    abstract fun bindAboutFeatureEntry(entry: AboutFeatureEntry): FeatureEntry
}
