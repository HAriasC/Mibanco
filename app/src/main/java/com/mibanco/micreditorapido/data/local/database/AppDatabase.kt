package com.mibanco.micreditorapido.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mibanco.micreditorapido.data.local.dao.CreditRequestDao
import com.mibanco.micreditorapido.data.local.model.CreditRequestEntity

@Database(entities = [CreditRequestEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creditRequestDao(): CreditRequestDao
}