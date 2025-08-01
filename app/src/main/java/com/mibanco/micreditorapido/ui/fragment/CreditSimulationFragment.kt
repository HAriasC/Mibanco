package com.mibanco.micreditorapido.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mibanco.micreditorapido.data.worker.CreditRequestRetryWorker
import com.mibanco.micreditorapido.databinding.FragmentCreditSimulationBinding
import com.mibanco.micreditorapido.domain.model.CreditRequestStatus
import com.mibanco.micreditorapido.ui.viewmodel.CreditSimulationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class CreditSimulationFragment : Fragment() {

    private lateinit var _binding: FragmentCreditSimulationBinding
    private val binding get() = _binding

    private val viewModel: CreditSimulationViewModel by viewModels()

    private val CLIENT_ID = "123456789"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditSimulationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
        viewModel.getCreditInfo(clientId = CLIENT_ID)
    }

    private fun setupObservers() {
        viewModel.creditInfo.observe(viewLifecycleOwner, Observer { creditInfo ->
            binding.apply {
                tvClientName.text = "Cliente: ${creditInfo.clientName}"
                tvCreditLine.text =
                    "Línea de Crédito Preaprobada: $${"%.2f".format(creditInfo.preApprovedAmount)}"

                sliderAmount.valueFrom = creditInfo.minAmount.toFloat()
                sliderAmount.valueTo = creditInfo.maxAmount.toFloat()
                sliderAmount.value = creditInfo.minAmount.toFloat()
                tvCurrentAmount.text = "$${"%.2f".format(creditInfo.minAmount)}"

                mainContentGroup.visibility = View.VISIBLE
            }
        })

        viewModel.creditSimulationResult.observe(viewLifecycleOwner, Observer { simulation ->
            binding.apply {
                tvSimulatedAmount.text = "Monto: $${"%.2f".format(simulation.amount)}"
                tvSimulatedTerm.text = "Plazo: ${simulation.termInMonths} meses"
                tvSimulatedInterest.text =
                    "Interés: ${"%.2f".format(simulation.interestRate)}%"
                tvSimulatedMonthlyPayment.text =
                    "Cuota Mensual: $${"%.2f".format(simulation.monthlyPayment)}"
                creditSummaryGroup.visibility = View.VISIBLE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.apply {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                btnSolicitarCredito.isEnabled = !isLoading
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                showStatusMessage(message = "Error: $errorMessage", isError = true)
            } else {
                binding.tvStatusMessage.visibility = View.GONE
            }
        })

        viewModel.creditRequestStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is CreditRequestStatus.Success -> {
                    showStatusMessage(
                        message = "Solicitud de crédito enviada con éxito!",
                        isError = false
                    )
                }

                is CreditRequestStatus.PendingOffline -> {
                    showStatusMessage(
                        message = "No hay conexión a internet. La solicitud se enviará automáticamente al restablecerse.",
                        isError = true
                    )
                    enqueueCreditRequestRetryWorker()
                }

                is CreditRequestStatus.Error -> {
                    showStatusMessage(
                        "Error al enviar la solicitud: ${status.message}",
                        isError = true
                    )
                }
            }
        })
    }

    private fun setupListeners() {
        binding.apply {
            sliderAmount.addOnChangeListener { slider, value, fromUser ->
                val selectedAmount = value.toDouble()
                tvCurrentAmount.text = "$${"%.2f".format(selectedAmount)}"
                val selectedTerm = etTermInMonths.text.toString().toIntOrNull() ?: 12
                if (selectedTerm > 0) {
                    viewModel.simulateCredit(amount = selectedAmount, termInMonths = selectedTerm)
                }
            }

            btnSolicitarCredito.setOnClickListener {
                val amount = sliderAmount.value.toDouble()
                val term = etTermInMonths.text.toString().toIntOrNull()
                if (term != null && term > 0) {
                    viewModel.sendCreditRequest(amount, term, CLIENT_ID)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Por favor, ingrese un plazo válido.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showStatusMessage(message: String, isError: Boolean) {
        binding.apply {
            tvStatusMessage.text = message
            tvStatusMessage.setTextColor(
                resources.getColor(
                    if (isError) android.R.color.holo_red_dark else android.R.color.holo_green_dark
                )
            )
            tvStatusMessage.visibility = View.VISIBLE
        }
    }

    private fun enqueueCreditRequestRetryWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val retryWorkRequest = OneTimeWorkRequest.Builder(
            workerClass = CreditRequestRetryWorker::class.java
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 3000L,
                timeUnit = TimeUnit.MILLISECONDS
            ).build()

        val workManager = WorkManager.getInstance(context = requireContext())
        workManager.enqueue(retryWorkRequest)
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(id = retryWorkRequest.id)

        workInfoLiveData.observe(viewLifecycleOwner, Observer { workInfo ->
            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                val successMessage = workInfo.outputData.getString(
                    key = CreditRequestRetryWorker.OUTPUT_MESSAGE_KEY
                )
                if (!successMessage.isNullOrEmpty()) {
                    showStatusMessage(successMessage, isError = false)
                }
            }
        })
    }
}