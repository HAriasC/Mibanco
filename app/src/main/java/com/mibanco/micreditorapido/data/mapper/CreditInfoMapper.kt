package com.mibanco.micreditorapido.data.mapper

import com.mibanco.micreditorapido.data.remote.dto.CreditInfoDTO
import com.mibanco.micreditorapido.domain.model.CreditInfo

object CreditInfoMapper {
    fun mapToDomain(dto: CreditInfoDTO): CreditInfo {
        return CreditInfo(
            clientName = dto.clientName,
            preApprovedAmount = dto.preApprovedAmount,
            minAmount = dto.minAmount,
            maxAmount = dto.maxAmount
        )
    }

    fun mapFromDomain(domain: CreditInfo): CreditInfoDTO {
        return CreditInfoDTO(
            clientName = domain.clientName,
            preApprovedAmount = domain.preApprovedAmount,
            minAmount = domain.minAmount,
            maxAmount = domain.maxAmount
        )
    }
}