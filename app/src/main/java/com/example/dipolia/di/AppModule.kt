package com.example.dipolia.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
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

private const val STREAMING_PREFERENCES_NAME = "streaming_preferences"

val Context.streamingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = STREAMING_PREFERENCES_NAME
)

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

    @Provides
    @Singleton
    fun provideUserDataStorePreferences(
        @ApplicationContext applicationContext: Context
    ): DataStore<Preferences> {
        return applicationContext.streamingDataStore
    }

    @Singleton
    @Provides
    fun provideRepository(dao: DipolsDao,
                          mapper: DipoliaMapper,
                          lampsRemoteDataSource: LampsRemoteDataSource,
                          sender: UDPClient,
                          dataStore: DataStore<Preferences>
    ): LampsRepository {
        return LampsRepositoryImpl(dao, mapper, lampsRemoteDataSource, sender, dataStore)
    }


    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
}