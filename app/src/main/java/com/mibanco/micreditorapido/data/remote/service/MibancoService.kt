package com.mibanco.micreditorapido.data.remote.service

import com.mibanco.micreditorapido.data.remote.dto.CreditInfoDTO
import com.mibanco.micreditorapido.data.remote.dto.CreditRequestDTO
import com.mibanco.micreditorapido.data.remote.dto.CreditResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MibancoService {
    @GET("clients/{clientId}/credit-info")
    suspend fun getCreditInfo(@Path("clientId") clientId: String): Response<CreditInfoDTO>

    @POST("credit-requests")
    suspend fun sendCreditRequest(@Body request: CreditRequestDTO): Response<CreditResponseDTO>
}