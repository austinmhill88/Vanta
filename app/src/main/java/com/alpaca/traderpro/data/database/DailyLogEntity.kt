package com.alpaca.traderpro.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val symbol: String,
    val highPrice: Double,
    val highTime: String,
    val lowPrice: Double,
    val lowTime: String,
    val diffDollar: Double,
    val diffPercent: Double
)
