package com.mibanco.micreditorapido.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mibanco.micreditorapido.domain.repository.CreditRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class CreditRequestRetryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val creditRepository: CreditRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val OUTPUT_MESSAGE_KEY = "output_message"
    }

    override suspend fun doWork(): Result = withContext(context = Dispatchers.IO) {
        val successMessage = creditRepository.retryPendingCreditRequests()
        if (successMessage != null) {
            val outputData = workDataOf(OUTPUT_MESSAGE_KEY to successMessage)
            return@withContext Result.success(outputData)
        }
        Result.success()
    }
}
