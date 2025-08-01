package com.mibanco.micreditorapido.data.mapper

import com.mibanco.micreditorapido.data.remote.dto.CreditResponseDTO
import com.mibanco.micreditorapido.domain.model.CreditResponse

object CreditResponseMapper {
    fun mapToDomain(dto: CreditResponseDTO): CreditResponse {
        return CreditResponse(
            status = dto.status,
            message = dto.message
        )
    }
}