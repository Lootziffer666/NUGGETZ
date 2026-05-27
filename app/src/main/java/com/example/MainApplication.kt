package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.ClaimRepository

class MainApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: ClaimRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "claims_database"
        ).build()
        repository = ClaimRepository(database.claimDao())
    }
}
