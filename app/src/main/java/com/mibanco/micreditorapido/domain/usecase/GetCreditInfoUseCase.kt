package com.mibanco.micreditorapido.domain.usecase

import com.mibanco.micreditorapido.domain.model.CreditInfo
import com.mibanco.micreditorapido.domain.repository.CreditRepository
import javax.inject.Inject

class GetCreditInfoUseCase @Inject constructor(
    private val creditRepository: CreditRepository
) {
    suspend fun execute(clientId: String): Result<CreditInfo> {
        return creditRepository.getCreditInfo(clientId)
    }
}