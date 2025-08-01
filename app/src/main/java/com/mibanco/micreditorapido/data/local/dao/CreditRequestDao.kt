package com.mibanco.micreditorapido.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mibanco.micreditorapido.data.local.model.CreditRequestEntity

@Dao
interface CreditRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditRequest(request: CreditRequestEntity)

    @Query("SELECT * FROM pending_credit_requests")
    suspend fun getAllPendingCreditRequests(): List<CreditRequestEntity>

    @Query("DELETE FROM pending_credit_requests WHERE id = :requestId")
    suspend fun deletePendingRequest(requestId: String)
}