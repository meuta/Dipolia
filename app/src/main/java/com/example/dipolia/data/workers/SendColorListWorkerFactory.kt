package com.example.dipolia.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.dipolia.data.network.UDPClient
import com.example.dipolia.domain.useCases.GetConnectedLampsUseCase
import javax.inject.Inject

class SendColorListWorkerFactory @Inject constructor(
    private val sender: UDPClient,
    private val getLampsUseCase: GetConnectedLampsUseCase
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
            getLampsUseCase
        )
    }
}