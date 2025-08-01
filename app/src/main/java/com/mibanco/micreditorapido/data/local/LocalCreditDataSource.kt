package com.mibanco.micreditorapido.data.local

import com.mibanco.micreditorapido.data.local.dao.CreditRequestDao
import com.mibanco.micreditorapido.data.mapper.CreditRequestMapper
import com.mibanco.micreditorapido.domain.model.CreditRequest
import javax.inject.Inject

class LocalCreditDataSource @Inject constructor(private val creditRequestDao: CreditRequestDao) {

    suspend fun saveCreditRequest(creditRequest: CreditRequest) {
        val entity = CreditRequestMapper.mapToEntity(creditRequest)
        creditRequestDao.insertCreditRequest(entity)
    }

    suspend fun getPendingCreditRequests(): List<CreditRequest> {
        return creditRequestDao.getAllPendingCreditRequests().map { entity ->
            CreditRequestMapper.mapToDomain(entity)
        }
    }

    suspend fun deletePendingRequest(requestId: String) {
        creditRequestDao.deletePendingRequest(requestId)
    }
}