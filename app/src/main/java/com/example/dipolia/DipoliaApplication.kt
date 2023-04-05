package com.example.dipolia

import android.app.Application
import com.example.dipolia.di.DaggerApplicationComponent

class DipoliaApplication: Application() {

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}