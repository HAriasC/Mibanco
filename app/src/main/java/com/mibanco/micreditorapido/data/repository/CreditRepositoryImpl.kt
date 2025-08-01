package com.mibanco.micreditorapido.data.repository

import com.mibanco.micreditorapido.data.local.LocalCreditDataSource
import com.mibanco.micreditorapido.data.mapper.CreditInfoMapper
import com.mibanco.micreditorapido.data.mapper.CreditRequestMapper
import com.mibanco.micreditorapido.data.mapper.CreditResponseMapper
import com.mibanco.micreditorapido.data.remote.service.MibancoService
import com.mibanco.micreditorapido.data.utils.NetworkUtils
import com.mibanco.micreditorapido.domain.model.CreditInfo
import com.mibanco.micreditorapido.domain.model.CreditRequest
import com.mibanco.micreditorapido.domain.model.CreditResponse
import com.mibanco.micreditorapido.domain.repository.CreditRepository
import javax.inject.Inject

class CreditRepositoryImpl @Inject constructor(
    private val apiService: MibancoService,
    val localDataSource: LocalCreditDataSource,
    private val networkUtils: NetworkUtils
) : CreditRepository {

    override suspend fun getCreditInfo(clientId: String): Result<CreditInfo> {
        return try {
            if (networkUtils.isOnline()) {
                val response = apiService.getCreditInfo(clientId = clientId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(value = CreditInfoMapper.mapToDomain(dto = response.body()!!))
                } else {
                    Result.failure(
                        exception = RuntimeException(
                            "Error al obtener información de crédito: ${response.code()} ${response.message()}"
                        )
                    )
                }
            } else {
                Result.failure(
                    exception = RuntimeException(
                        "No hay conexión a internet para obtener la información de crédito."
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(exception = e)
        }
    }

    override suspend fun sendCreditRequest(creditRequest: CreditRequest): Result<CreditResponse> {
        return try {
            if (networkUtils.isOnline()) {
                val requestDTO = CreditRequestMapper.mapToDto(domain = creditRequest)
                val response = apiService.sendCreditRequest(request = requestDTO)
                if (response.isSuccessful && response.body() != null) {
                    localDataSource.deletePendingRequest(requestId = creditRequest.id)
                    Result.success(value = CreditResponseMapper.mapToDomain(dto = response.body()!!))
                } else {
                    localDataSource.saveCreditRequest(creditRequest = creditRequest)
                    Result.failure(
                        exception = RuntimeException(
                            "Error al enviar la solicitud: ${response.code()} ${response.message()}"
                        )
                    )
                }
            } else {
                localDataSource.saveCreditRequest(creditRequest)
                Result.failure(
                    exception = RuntimeException(
                        "No hay conexión a internet. Solicitud guardada para reintento."
                    )
                )
            }
        } catch (e: Exception) {
            localDataSource.saveCreditRequest(creditRequest)
            Result.failure(exception = e)
        }
    }

    override suspend fun retryPendingCreditRequests(): String? {
        if (networkUtils.isOnline()) {
            val pendingRequests = localDataSource.getPendingCreditRequests()
            if (pendingRequests.isEmpty()) {
                return null
            }
            var successfulRetries = 0
            pendingRequests.forEach { request ->
                try {
                    val requestDTO = CreditRequestMapper.mapToDto(request)
                    val response = apiService.sendCreditRequest(requestDTO)
                    if (response.isSuccessful) {
                        localDataSource.deletePendingRequest(request.id)
                        successfulRetries++
                    }
                } catch (e: Exception) {

                }
            }
            return if (successfulRetries > 0) {
                "Se enviaron $successfulRetries solicitudes de crédito pendientes con éxito."
            } else {
                null
            }
        }
        return null
    }
}