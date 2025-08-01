package com.mibanco.micreditorapido.data.mockserver

import com.mibanco.micreditorapido.data.remote.dto.CreditInfoDTO
import com.mibanco.micreditorapido.data.remote.dto.CreditRequestDTO
import com.mibanco.micreditorapido.data.remote.dto.CreditResponseDTO
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(factory = Netty, port = 8080) {
        install(plugin = ContentNegotiation) {
            gson {

            }
        }

        routing {
            get(path = "/") {
                call.respondText(
                    text = "Mibanco Service"
                )
                staticResources("/static", "static")
            }
            // Endpoint para obtener la información de crédito
            get(path = "/clients/{clientId}/credit-info") {
                val clientId = call.parameters["clientId"]
                if (clientId == null) {
                    call.respond(status = HttpStatusCode.BadRequest, message = "Client ID is required")
                    return@get
                }

                // Simula una respuesta exitosa
                val creditInfo = CreditInfoDTO(
                    clientName = "Cliente $clientId",
                    preApprovedAmount = 15000.00,
                    minAmount = 1000.00,
                    maxAmount = 15000.00
                )
                call.respond(status = HttpStatusCode.OK, message = creditInfo)
            }

            // Endpoint para enviar la solicitud de crédito
            post(path = "/credit-requests") {
                try {
                    val request = call.receive<CreditRequestDTO>()

                    // Simula un error para un cliente específico
                    if (request.clientId == "client_error") {
                        val errorResponse = CreditResponseDTO(
                            status = "error",
                            message = "El monto solicitado excede el límite."
                        )
                        call.respond(status = HttpStatusCode.BadRequest, message = errorResponse)
                        return@post
                    }

                    // Simula una respuesta exitosa
                    val successResponse = CreditResponseDTO(
                        status = "success",
                        message = "Solicitud de crédito procesada con éxito."
                    )
                    call.respond(status = HttpStatusCode.OK, message = successResponse)

                } catch (e: Exception) {
                    val errorResponse = CreditResponseDTO(
                        status = "error",
                        message = "Error en el formato de la solicitud: ${e.message}"
                    )
                    call.respond(status = HttpStatusCode.BadRequest, message = errorResponse)
                }
            }
        }
    }.start(wait = true)
}