package com.mibanco.micreditorapido.domain.usecase

import com.mibanco.micreditorapido.domain.model.CreditRequest
import com.mibanco.micreditorapido.domain.model.CreditResponse
import com.mibanco.micreditorapido.domain.repository.CreditRepository
import java.util.UUID
import javax.inject.Inject

class SendCreditRequestUseCase @Inject constructor(
    private val creditRepository: CreditRepository
) {
    suspend fun execute(amount: Double, termInMonths: Int, clientId: String): Result<CreditResponse> {
        val requestId = UUID.randomUUID().toString()
        val requestDate = System.currentTimeMillis()
        val creditRequest = CreditRequest(requestId, amount, termInMonths, clientId, requestDate)

        return creditRepository.sendCreditRequest(creditRequest)
    }
}