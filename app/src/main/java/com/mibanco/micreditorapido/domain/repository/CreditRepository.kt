package com.mibanco.micreditorapido.domain.repository

import com.mibanco.micreditorapido.domain.model.CreditInfo
import com.mibanco.micreditorapido.domain.model.CreditRequest
import com.mibanco.micreditorapido.domain.model.CreditResponse

interface CreditRepository {
    suspend fun getCreditInfo(clientId: String): Result<CreditInfo>
    suspend fun sendCreditRequest(creditRequest: CreditRequest): Result<CreditResponse>
    suspend fun retryPendingCreditRequests(): String?
}