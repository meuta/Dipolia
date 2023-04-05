package com.example.dipolia.di

import android.app.Application
import com.example.dipolia.data.LampsRepositoryImpl
import com.example.dipolia.data.database.AppDatabase
import com.example.dipolia.data.database.DipolsDao
import com.example.dipolia.domain.LampsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindRepository(impl: LampsRepositoryImpl): LampsRepository

    companion object{

        @ApplicationScope
        @Provides
        fun provideDipolsDao(
            application: Application
        ): DipolsDao{
            return AppDatabase.getInstance(application).dipolsDao()
        }
    }
}