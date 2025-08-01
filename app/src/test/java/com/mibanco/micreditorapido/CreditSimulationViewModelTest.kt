package com.mibanco.micreditorapido

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mibanco.micreditorapido.domain.model.CreditInfo
import com.mibanco.micreditorapido.domain.model.CreditRequestStatus
import com.mibanco.micreditorapido.domain.model.CreditResponse
import com.mibanco.micreditorapido.domain.model.CreditSimulation
import com.mibanco.micreditorapido.domain.usecase.GetCreditInfoUseCase
import com.mibanco.micreditorapido.domain.usecase.SendCreditRequestUseCase
import com.mibanco.micreditorapido.domain.usecase.SimulateCreditUseCase
import com.mibanco.micreditorapido.ui.viewmodel.CreditSimulationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CreditSimulationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getCreditInfoUseCase: GetCreditInfoUseCase
    private lateinit var simulateCreditUseCase: SimulateCreditUseCase
    private lateinit var sendCreditRequestUseCase: SendCreditRequestUseCase

    private lateinit var viewModel: CreditSimulationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCreditInfoUseCase = Mockito.mock(GetCreditInfoUseCase::class.java)
        simulateCreditUseCase = Mockito.mock(SimulateCreditUseCase::class.java)
        sendCreditRequestUseCase = Mockito.mock(SendCreditRequestUseCase::class.java)

        viewModel = CreditSimulationViewModel(
            getCreditInfoUseCase,
            simulateCreditUseCase,
            sendCreditRequestUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getCreditInfo establece la carga y luego el éxito con los datos correctos`() = runTest {
        val clientId = "testClient"
        val expectedCreditInfo = CreditInfo("Test Client", 10000.0, 1000.0, 10000.0)
        val expectedSimulation = CreditSimulation(1000.0, 12, 5.0, 85.0)

        `when`(getCreditInfoUseCase.execute(clientId)).thenReturn(Result.success(expectedCreditInfo))
        `when`(simulateCreditUseCase.execute(anyDouble(), anyInt())).thenReturn(expectedSimulation)

        viewModel.getCreditInfo(clientId)

        assertTrue(viewModel.isLoading.value ?: false)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(getCreditInfoUseCase).execute(clientId)
        assertFalse(viewModel.isLoading.value ?: false)
        assertEquals(expectedCreditInfo, viewModel.creditInfo.value)
        verify(simulateCreditUseCase).execute(expectedCreditInfo.minAmount, 12)
        assertEquals(expectedSimulation, viewModel.creditSimulationResult.value)
    }

    @Test
    fun `getCreditInfo devuelve un error cuando falla el caso de uso`() = runTest {
        val clientId = "testClient"
        val errorMessage = "Network Error"
        `when`(getCreditInfoUseCase.execute(clientId)).thenReturn(Result.failure(RuntimeException(errorMessage)))

        viewModel.getCreditInfo(clientId)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoading.value ?: false)
        assertEquals(errorMessage, viewModel.error.value)
        assertNull(viewModel.creditInfo.value)
        assertNull(viewModel.creditSimulationResult.value)
        verify(getCreditInfoUseCase).execute(clientId)
    }

    @Test
    fun `simulateCredit calcula la simulación y actualiza LiveData`() = runTest {
        val amount = 5000.0
        val term = 24
        val expectedSimulation = CreditSimulation(amount, term, 5.0, 220.0)

        `when`(simulateCreditUseCase.execute(amount, term)).thenReturn(expectedSimulation)

        viewModel.simulateCredit(amount, term)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoading.value ?: false)
        assertEquals(expectedSimulation, viewModel.creditSimulationResult.value)
        verify(simulateCreditUseCase).execute(amount, term)
    }

    @Test
    fun `sendCreditRequest establece la carga y luego el éxito cuando el caso de uso es exitoso`() = runTest {
        val amount = 5000.0
        val term = 24
        val clientId = "testClient"
        val successResponse = CreditResponse("success", "Solicitud exitosa")

        `when`(sendCreditRequestUseCase.execute(amount, term, clientId)).thenReturn(Result.success(successResponse))

        viewModel.sendCreditRequest(amount, term, clientId)

        assertTrue(viewModel.isLoading.value ?: false)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoading.value ?: false)
        verify(sendCreditRequestUseCase).execute(amount, term, clientId)
        assertIs<CreditRequestStatus.Success>(viewModel.creditRequestStatus.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `sendCreditRequest establece el estado como pendiente cuando no hay conexión a Internet`() = runTest {
        val amount = 5000.0
        val term = 24
        val clientId = "testClient"
        val offlineException = RuntimeException("No hay conexión a internet. Solicitud guardada para reintento.")

        `when`(sendCreditRequestUseCase.execute(amount, term, clientId)).thenReturn(Result.failure(offlineException))

        viewModel.sendCreditRequest(amount, term, clientId)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoading.value ?: false)
        verify(sendCreditRequestUseCase).execute(amount, term, clientId)
        assertIs<CreditRequestStatus.PendingOffline>(viewModel.creditRequestStatus.value)
        assertEquals(offlineException.message, viewModel.error.value)
    }

    @Test
    fun `sendCreditRequest establece el estado como error para otras excepciones`() = runTest {
        val amount = 5000.0
        val term = 24
        val clientId = "testClient"
        val apiException = RuntimeException("API Error: Bad Request")

        `when`(sendCreditRequestUseCase.execute(amount, term, clientId)).thenReturn(Result.failure(apiException))

        viewModel.sendCreditRequest(amount, term, clientId)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoading.value ?: false)
        verify(sendCreditRequestUseCase).execute(amount, term, clientId)
        assertIs<CreditRequestStatus.Error>(viewModel.creditRequestStatus.value)
        assertEquals(apiException.message, viewModel.error.value)
    }
}