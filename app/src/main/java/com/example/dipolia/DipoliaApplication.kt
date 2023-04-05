package com.example.dipolia

import android.app.Application
import androidx.work.Configuration
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.data.workers.SendColorListWorkerFactory
import com.example.dipolia.di.DaggerApplicationComponent
import javax.inject.Inject

class DipoliaApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: SendColorListWorkerFactory

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun onCreate() {
        component.inject(this)
        super.onCreate()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()

            .setWorkerFactory(workerFactory)
            .build()
    }
}