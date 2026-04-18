package com.example.rooming.di

import com.example.rooming.data.bookings.FakeBookingRepository
import com.example.rooming.data.favorites.FakeFavoritesRepository
import com.example.rooming.data.rooms.FakeRoomsRepository
import com.example.rooming.domain.repository.BookingRepository
import com.example.rooming.domain.repository.FavoritesRepository
import com.example.rooming.domain.repository.RoomsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRoomsRepository(repository: FakeRoomsRepository): RoomsRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(repository: FakeFavoritesRepository): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(repository: FakeBookingRepository): BookingRepository
}
