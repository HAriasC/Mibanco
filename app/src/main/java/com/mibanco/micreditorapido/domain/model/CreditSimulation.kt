package com.mibanco.micreditorapido.domain.model

data class CreditSimulation(
    val amount: Double,
    val termInMonths: Int,
    val interestRate: Double,
    val monthlyPayment: Double
)