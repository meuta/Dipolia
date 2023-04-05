package com.example.dipolia

import android.app.Application
import androidx.work.Configuration
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.data.workers.SendColorListWorkerFactory
import com.example.dipolia.di.DaggerApplicationComponent

class DipoliaApplication: Application(), Configuration.Provider {

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(
                SendColorListWorkerFactory(
                    UDPClient()
                )
            )
            .build()
    }
}