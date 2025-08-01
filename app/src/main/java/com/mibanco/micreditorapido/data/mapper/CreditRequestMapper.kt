package com.mibanco.micreditorapido.data.mapper

import com.mibanco.micreditorapido.data.local.model.CreditRequestEntity
import com.mibanco.micreditorapido.data.remote.dto.CreditRequestDTO
import com.mibanco.micreditorapido.domain.model.CreditRequest

object CreditRequestMapper {
    fun mapToDto(domain: CreditRequest): CreditRequestDTO {
        return CreditRequestDTO(
            amount = domain.amount,
            termInMonths = domain.termInMonths,
            clientId = domain.clientId
        )
    }

    fun mapToEntity(domain: CreditRequest): CreditRequestEntity {
        return CreditRequestEntity(
            id = domain.id,
            amount = domain.amount,
            termInMonths = domain.termInMonths,
            clientId = domain.clientId,
            requestDate = domain.requestDate
        )
    }

    fun mapToDomain(entity: CreditRequestEntity): CreditRequest {
        return CreditRequest(
            id = entity.id,
            amount = entity.amount,
            termInMonths = entity.termInMonths,
            clientId = entity.clientId,
            requestDate = entity.requestDate
        )
    }
}