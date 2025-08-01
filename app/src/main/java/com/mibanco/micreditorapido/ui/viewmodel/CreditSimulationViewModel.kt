package com.mibanco.micreditorapido.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mibanco.micreditorapido.domain.model.CreditInfo
import com.mibanco.micreditorapido.domain.model.CreditRequestStatus
import com.mibanco.micreditorapido.domain.model.CreditSimulation
import com.mibanco.micreditorapido.domain.usecase.GetCreditInfoUseCase
import com.mibanco.micreditorapido.domain.usecase.SendCreditRequestUseCase
import com.mibanco.micreditorapido.domain.usecase.SimulateCreditUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditSimulationViewModel @Inject constructor(
    private val getCreditInfoUseCase: GetCreditInfoUseCase,
    private val simulateCreditUseCase: SimulateCreditUseCase,
    private val sendCreditRequestUseCase: SendCreditRequestUseCase
) : ViewModel() {

    private val _creditInfo = MutableLiveData<CreditInfo>()
    val creditInfo: LiveData<CreditInfo> = _creditInfo

    private val _creditSimulationResult = MutableLiveData<CreditSimulation>()
    val creditSimulationResult: LiveData<CreditSimulation> = _creditSimulationResult

    private val _creditRequestStatus = MutableLiveData<CreditRequestStatus>()
    val creditRequestStatus: LiveData<CreditRequestStatus> = _creditRequestStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getCreditInfo(clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getCreditInfoUseCase.execute(clientId)
                .onSuccess { info ->
                    _creditInfo.value = info
                    simulateCredit(info.minAmount, termInMonths = 12)
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            _isLoading.value = false
        }
    }

    fun simulateCredit(amount: Double, termInMonths: Int) {
        _isLoading.value = true
        try {
            val simulatedCredit = simulateCreditUseCase.execute(amount, termInMonths)
            _creditSimulationResult.value = simulatedCredit
        } catch (e: Exception) {
            _error.value = "Error al simular el crédito: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun sendCreditRequest(amount: Double, termInMonths: Int, clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            sendCreditRequestUseCase.execute(amount, termInMonths, clientId)
                .onSuccess { response ->
                    _creditRequestStatus.value = CreditRequestStatus.Success
                }
                .onFailure { exception ->
                    if (exception.message?.contains(other = "No hay conexión") == true) {
                        _creditRequestStatus.value = CreditRequestStatus.PendingOffline
                    } else {
                        _creditRequestStatus.value = CreditRequestStatus.Error(
                            message = exception.message ?: "Error desconocido"
                        )
                        _error.value = exception.message
                    }
                }
            _isLoading.value = false
        }
    }
}