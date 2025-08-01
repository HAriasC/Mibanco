package com.mibanco.micreditorapido.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreditResponseDTO(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)