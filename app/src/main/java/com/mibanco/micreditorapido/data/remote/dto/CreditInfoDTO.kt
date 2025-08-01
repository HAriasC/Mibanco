package com.mibanco.micreditorapido.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreditInfoDTO(
    @SerializedName("client_name") val clientName: String,
    @SerializedName("pre_approved_amount") val preApprovedAmount: Double,
    @SerializedName("min_amount") val minAmount: Double,
    @SerializedName("max_amount") val maxAmount: Double
)