package com.mibanco.micreditorapido

import com.mibanco.micreditorapido.data.local.LocalCreditDataSource
import com.mibanco.micreditorapido.data.mapper.CreditRequestMapper
import com.mibanco.micreditorapido.data.remote.dto.CreditInfoDTO
import com.mibanco.micreditorapido.data.remote.dto.CreditResponseDTO
import com.mibanco.micreditorapido.data.remote.service.MibancoService
import com.mibanco.micreditorapido.data.repository.CreditRepositoryImpl
import com.mibanco.micreditorapido.data.utils.NetworkUtils
import com.mibanco.micreditorapido.domain.model.CreditRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CreditRepositoryImplTest {

    private lateinit var apiService: MibancoService
    private lateinit var localDataSource: LocalCreditDataSource
    private lateinit var networkUtils: NetworkUtils
    private lateinit var repository: CreditRepositoryImpl

    @Before
    fun setup() {
        apiService = Mockito.mock(MibancoService::class.java)
        localDataSource = Mockito.mock(LocalCreditDataSource::class.java)
        networkUtils = Mockito.mock(NetworkUtils::class.java)
        repository = CreditRepositoryImpl(apiService, localDataSource, networkUtils)
    }

    @Test
    fun `getCreditInfo devuelve datos cuando está en línea y la llamada al API es exitosa`() =
        runTest {
            val clientId = "testClient"
            val creditInfoDTO = CreditInfoDTO(
                clientName = "testClient",
                preApprovedAmount = 10000.0,
                minAmount = 1000.0,
                maxAmount = 10000.0
            )
            val successResponse = Response.success(creditInfoDTO)

            `when`(networkUtils.isOnline()).thenReturn(true)
            `when`(apiService.getCreditInfo(clientId = clientId)).thenReturn(successResponse)

            val result = repository.getCreditInfo(clientId = clientId)

            assertTrue(actual = result.isSuccess)
            assertEquals(expected = "testClient", actual = result.getOrNull()?.clientName)
            verify(apiService).getCreditInfo(clientId = clientId)
            verifyNoInteractions(localDataSource)
        }

    @Test
    fun `getCreditInfo devuelve un error cuando está sin conexión`() = runTest {
        val clientId = "testClient"
        `when`(networkUtils.isOnline()).thenReturn(false)

        val result = repository.getCreditInfo(clientId = clientId)

        assertTrue(actual = result.isFailure)
        assertIs<RuntimeException>(value = result.exceptionOrNull())
        assertEquals(
            expected = "No hay conexión a internet para obtener la información de crédito.",
            actual = result.exceptionOrNull()?.message
        )
        verifyNoInteractions(apiService)
        verifyNoInteractions(localDataSource)
    }

    @Test
    fun `sendCreditRequest guarda localmente y devuelve un error cuando está sin conexión`() =
        runTest {
            val creditRequest = CreditRequest(
                id = "req1",
                amount = 5000.0,
                termInMonths = 24,
                clientId = "client1",
                requestDate = 123456L
            )

            `when`(networkUtils.isOnline()).thenReturn(false)

            val result = repository.sendCreditRequest(creditRequest = creditRequest)

            assertTrue(actual = result.isFailure)
            assertIs<RuntimeException>(value = result.exceptionOrNull())
            assertEquals(
                expected = "No hay conexión a internet. Solicitud guardada para reintento.",
                actual = result.exceptionOrNull()?.message
            )
            verify(localDataSource).saveCreditRequest(creditRequest)
            verifyNoInteractions(apiService)
        }

    @Test
    fun `sendCreditRequest envía al API y borra localmente cuando está en línea y es exitoso`() =
        runTest {
            val creditRequest = CreditRequest(
                id = "req1",
                amount = 5000.0,
                termInMonths = 24,
                clientId = "client1",
                requestDate = 123456L
            )
            val creditRequestDTO = CreditRequestMapper.mapToDto(domain = creditRequest)
            val creditResponseDTO = CreditResponseDTO(
                status = "success",
                message = "Solicitud procesada"
            )
            val successResponse = Response.success(creditResponseDTO)

            `when`(networkUtils.isOnline()).thenReturn(true)
            `when`(
                apiService.sendCreditRequest(
                    request = creditRequestDTO
                )
            ).thenReturn(successResponse)

            val result = repository.sendCreditRequest(creditRequest = creditRequest)

            assertTrue(actual = result.isSuccess)
            assertEquals(expected = "success", actual = result.getOrNull()?.status)
            verify(apiService).sendCreditRequest(request = creditRequestDTO)
            verify(localDataSource).deletePendingRequest(requestId = creditRequest.id)
        }

    @Test
    fun `sendCreditRequest guarda localmente y devuelve un error cuando está en línea, pero el API falla`() =
        runTest {
            val creditRequest = CreditRequest(
                id = "req1",
                amount = 5000.0,
                termInMonths = 24,
                clientId = "client1",
                requestDate = 123456L
            )
            val creditRequestDTO = CreditRequestMapper.mapToDto(domain = creditRequest)
            val errorResponse = Response.error<CreditResponseDTO>(
                400,
                "Bad Request".toResponseBody(contentType = "application/json".toMediaTypeOrNull())
            )

            `when`(networkUtils.isOnline()).thenReturn(true)
            `when`(
                apiService.sendCreditRequest(
                    request = creditRequestDTO
                )
            ).thenReturn(errorResponse)

            val result = repository.sendCreditRequest(creditRequest = creditRequest)

            assertTrue(actual = result.isFailure)
            assertIs<RuntimeException>(value = result.exceptionOrNull())
            assertTrue(
                actual = result.exceptionOrNull()?.message?.contains(
                    other = "Error al enviar la solicitud"
                ) ?: false
            )
            verify(apiService).sendCreditRequest(creditRequestDTO)
            verify(localDataSource).saveCreditRequest(creditRequest = creditRequest)
        }
}