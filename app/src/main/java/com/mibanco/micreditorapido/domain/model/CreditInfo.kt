package com.mibanco.micreditorapido.domain.model

data class CreditInfo(
    val clientName: String,
    val preApprovedAmount: Double,
    val minAmount: Double,
    val maxAmount: Double
)
