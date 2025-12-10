package com.alpaca.traderpro.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auto_trade_logs")
data class AutoTradeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val symbol: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val targetPrice: Double,
    val stopPrice: Double,
    val quantity: Int,
    val entryTime: String,
    val exitTime: String,
    val profitLoss: Double,
    val profitLossPercent: Double,
    val exitReason: String, // "TARGET", "STOP", "FORCE_EXIT", "MANUAL"
    val signalType: String = "SHORT"
)
