package com.example.dipolia.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import com.example.dipolia.presentation.LocalModeViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(LocalModeViewModel::class)
    fun bindLocalModeViewModel(viewModel: LocalModeViewModel): ViewModel

    companion object{

        @ApplicationScope
        @Provides
        fun provideWorkManager(
            application: Application
        ): WorkManager {
            return WorkManager.getInstance(application)
        }
    }

}