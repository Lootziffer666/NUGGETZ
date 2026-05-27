package com.example.data

import kotlinx.coroutines.flow.Flow

class ClaimRepository(private val claimDao: ClaimDao) {
    val allClaims: Flow<List<Claim>> = claimDao.getAllClaims()
    val pendingClaims: Flow<List<Claim>> = claimDao.getPendingClaims()

    suspend fun insert(claim: Claim) = claimDao.insertClaim(claim)
    
    suspend fun insertAll(claims: List<Claim>) = claimDao.insertAll(claims)

    suspend fun update(claim: Claim) = claimDao.updateClaim(claim)

    suspend fun deleteAll() = claimDao.deleteAll()
    
    suspend fun count(): Int = claimDao.countClaims()
}
