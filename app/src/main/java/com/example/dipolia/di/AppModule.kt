package com.example.dipolia.di

import android.content.Context
import androidx.work.WorkManager
import com.example.dipolia.data.LampsRepositoryImpl
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.database.DipolsDao
import com.example.dipolia.data.mapper.DipoliaMapper
import com.example.dipolia.data.network.*
import com.example.dipolia.domain.LampsRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideDipolsDao(db: AppDatabase): DipolsDao {
        return db.dipolsDao()
    }

    @Singleton
    @Provides
    fun provideMapper(): DipoliaMapper {
        return DipoliaMapper()
    }

    @Singleton
    @Provides
    fun provideLampsApi(receiver: UDPServer): LampsApi {
        return LampsApiImpl(receiver)
    }

    @Singleton
    @Provides
    fun provideLampsRemoteDataSource(lampsApi: LampsApi): LampsRemoteDataSource {
        return LampsRemoteDataSource(lampsApi)
    }

    @Singleton
    @Provides
    fun provideSender(): UDPClient {
        return UDPClient()
    }

    @Singleton
    @Provides
    fun provideRepository(dao: DipolsDao,
                          mapper: DipoliaMapper,
                          lampsRemoteDataSource: LampsRemoteDataSource,
                          sender: UDPClient
    ): LampsRepository {
        return LampsRepositoryImpl(dao, mapper, lampsRemoteDataSource, sender)
    }


    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
}