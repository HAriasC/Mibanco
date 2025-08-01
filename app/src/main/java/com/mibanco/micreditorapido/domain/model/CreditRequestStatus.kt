package com.mibanco.micreditorapido.domain.model

sealed class CreditRequestStatus {
    object Success : CreditRequestStatus()
    object PendingOffline : CreditRequestStatus()
    data class Error(val message: String) : CreditRequestStatus()
}