package com.example.rooming.feature.bookings.impl

import com.example.rooming.core.navigation.FeatureEntry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingsFeatureModule {
    @Binds
    @IntoSet
    abstract fun bindBookingsFeatureEntry(entry: BookingsFeatureEntry): FeatureEntry
}
