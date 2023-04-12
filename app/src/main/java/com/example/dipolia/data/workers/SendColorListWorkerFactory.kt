package com.example.dipolia.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.dipolia.data.LampsRepositoryImpl
import com.example.dipolia.data.network.UDPClient
import javax.inject.Inject

class SendColorListWorkerFactory @Inject constructor(
    private val sender: UDPClient,
    private val repositoryImpl: LampsRepositoryImpl
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {

        return SendColorListWorker(
            appContext,
            workerParameters,
            sender,
            repositoryImpl
        )
    }
}