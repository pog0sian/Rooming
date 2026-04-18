package com.example.rooming.feature.rooms.impl

import com.example.rooming.core.navigation.FeatureEntry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class RoomsFeatureModule {
    @Binds
    @IntoSet
    abstract fun bindRoomsFeatureEntry(entry: RoomsFeatureEntry): FeatureEntry
}
