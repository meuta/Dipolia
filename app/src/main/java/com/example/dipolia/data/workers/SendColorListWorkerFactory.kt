package com.example.dipolia.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.dipolia.data.network.UDPClient

class SendColorListWorkerFactory(
    private val sender: UDPClient
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {

        return SendColorListWorker(
            appContext,
            workerParameters,
            sender
        )
    }
}