package com.mibanco.micreditorapido.data.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.mibanco.micreditorapido.data.local.dao.CreditRequestDao
import com.mibanco.micreditorapido.data.local.database.AppDatabase
import com.mibanco.micreditorapido.data.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "mibanco-database"
        ).build()
    }

    @Provides
    fun provideCreditRequestDao(appDatabase: AppDatabase): CreditRequestDao {
        return appDatabase.creditRequestDao()
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext appContext: Context): NetworkUtils {
        return NetworkUtils(appContext)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}