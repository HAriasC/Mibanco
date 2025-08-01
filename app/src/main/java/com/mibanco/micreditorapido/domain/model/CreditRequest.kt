package com.mibanco.micreditorapido.domain.model

data class CreditRequest(
    val id: String,
    val amount: Double,
    val termInMonths: Int,
    val clientId: String,
    val requestDate: Long
)