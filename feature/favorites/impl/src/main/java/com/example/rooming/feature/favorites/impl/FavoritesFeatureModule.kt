package com.example.rooming.feature.favorites.impl

import com.example.rooming.core.navigation.FeatureEntry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesFeatureModule {
    @Binds
    @IntoSet
    abstract fun bindFavoritesFeatureEntry(entry: FavoritesFeatureEntry): FeatureEntry
}
