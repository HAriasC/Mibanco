package com.mibanco.micreditorapido.domain.model

sealed class CreditUiState {
    object Idle : CreditUiState()
    object Loading : CreditUiState()
    data class Success(val message: String) : CreditUiState()
    data class Error(val message: String) : CreditUiState()
    data class CreditLineLoaded(val creditInfo: CreditInfo) : CreditUiState()
}