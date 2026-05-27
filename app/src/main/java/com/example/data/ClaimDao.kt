package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims")
    fun getAllClaims(): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE status = 'PENDING'")
    fun getPendingClaims(): Flow<List<Claim>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: Claim)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(claims: List<Claim>)

    @Update
    suspend fun updateClaim(claim: Claim)

    @Query("DELETE FROM claims")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM claims")
    suspend fun countClaims(): Int
}
