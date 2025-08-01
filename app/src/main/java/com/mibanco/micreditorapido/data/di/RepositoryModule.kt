package com.mibanco.micreditorapido.data.di

import com.mibanco.micreditorapido.data.local.LocalCreditDataSource
import com.mibanco.micreditorapido.data.remote.service.MibancoService
import com.mibanco.micreditorapido.data.repository.CreditRepositoryImpl
import com.mibanco.micreditorapido.data.utils.NetworkUtils
import com.mibanco.micreditorapido.domain.repository.CreditRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCreditRepository(
        apiService: MibancoService,
        localDataSource: LocalCreditDataSource,
        networkUtils: NetworkUtils
    ): CreditRepository {
        return CreditRepositoryImpl(apiService, localDataSource, networkUtils)
    }
}