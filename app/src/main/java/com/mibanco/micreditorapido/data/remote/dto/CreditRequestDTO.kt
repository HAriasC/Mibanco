package com.mibanco.micreditorapido.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreditRequestDTO(
    @SerializedName("amount") val amount: Double,
    @SerializedName("term_in_months") val termInMonths: Int,
    @SerializedName("client_id") val clientId: String
)