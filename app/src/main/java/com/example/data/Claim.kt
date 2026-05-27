package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "claims")
data class Claim(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val claimId: String,
    val content: String,
    val sourceFile: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, CONFLICT
    val clusterId: String? = null,
    val aiAnalysis: String? = null
)
