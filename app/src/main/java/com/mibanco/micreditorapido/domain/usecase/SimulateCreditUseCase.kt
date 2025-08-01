package com.mibanco.micreditorapido.domain.usecase

import com.mibanco.micreditorapido.domain.model.CreditSimulation
import javax.inject.Inject
import kotlin.math.pow

class SimulateCreditUseCase @Inject constructor() {
    fun execute(amount: Double, termInMonths: Int, interestRate: Double = 0.05): CreditSimulation {
        val monthlyInterestRate = interestRate / 12
        val monthlyPayment =
            (amount * monthlyInterestRate) / (1 - (1 + monthlyInterestRate).pow(-termInMonths.toDouble()))

        return CreditSimulation(
            amount = amount,
            termInMonths = termInMonths,
            interestRate = interestRate * 100,
            monthlyPayment = monthlyPayment
        )
    }
}