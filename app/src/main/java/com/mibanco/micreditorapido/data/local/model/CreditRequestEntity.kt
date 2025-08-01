package com.mibanco.micreditorapido.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_credit_requests")
data class CreditRequestEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val termInMonths: Int,
    val clientId: String,
    val requestDate: Long = System.currentTimeMillis()
)
