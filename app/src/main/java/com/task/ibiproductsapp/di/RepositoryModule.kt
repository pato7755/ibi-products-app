package com.task.ibiproductsapp.di

import com.task.ibiproductsapp.data.local.dao.FavoriteDao
import com.task.ibiproductsapp.data.local.dao.ProductDao
import com.task.ibiproductsapp.data.remote.ApiService
import com.task.ibiproductsapp.data.repository.AuthRepositoryImpl
import com.task.ibiproductsapp.data.repository.ProductRepositoryImpl
import com.task.ibiproductsapp.domain.repository.AuthRepository
import com.task.ibiproductsapp.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService
    ): AuthRepository = AuthRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideProductRepository(
        apiService: ApiService,
        productDao: ProductDao,
        favoriteDao: FavoriteDao
    ): ProductRepository = ProductRepositoryImpl(apiService, productDao, favoriteDao)
}

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}