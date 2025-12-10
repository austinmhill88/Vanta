package com.alpaca.traderpro.data.repository

import com.alpaca.traderpro.data.database.AutoTradeLogDao
import com.alpaca.traderpro.data.database.AutoTradeLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AutoTradeLogsRepository(
    private val autoTradeLogDao: AutoTradeLogDao
) {
    
    fun getAllAutoTradeLogs(): Flow<List<AutoTradeLogEntity>> {
        return autoTradeLogDao.getAllAutoTradeLogs()
    }
    
    fun getAutoTradeLogsBySymbol(symbol: String): Flow<List<AutoTradeLogEntity>> {
        return autoTradeLogDao.getAutoTradeLogsBySymbol(symbol)
    }
    
    suspend fun saveAutoTradeLog(
        symbol: String,
        entryPrice: Double,
        exitPrice: Double,
        targetPrice: Double,
        stopPrice: Double,
        quantity: Int,
        entryTime: LocalTime,
        exitTime: LocalTime,
        exitReason: String,
        signalType: String = "SHORT"
    ) {
        val profitLoss = (entryPrice - exitPrice) * quantity // For SHORT
        val profitLossPercent = ((entryPrice - exitPrice) / entryPrice) * 100
        
        val log = AutoTradeLogEntity(
            date = LocalDate.now().toString(),
            symbol = symbol,
            entryPrice = entryPrice,
            exitPrice = exitPrice,
            targetPrice = targetPrice,
            stopPrice = stopPrice,
            quantity = quantity,
            entryTime = entryTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            exitTime = exitTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            profitLoss = profitLoss,
            profitLossPercent = profitLossPercent,
            exitReason = exitReason,
            signalType = signalType
        )
        
        autoTradeLogDao.insertAutoTradeLog(log)
    }
    
    suspend fun getAutoTradeCountForToday(): Int {
        val today = LocalDate.now().toString()
        return autoTradeLogDao.getAutoTradeCountForDate(today)
    }
    
    suspend fun getTodayTotalProfitLoss(): Double {
        val today = LocalDate.now().toString()
        return autoTradeLogDao.getTotalProfitLossForDate(today) ?: 0.0
    }
    
    suspend fun getWinRate(): Pair<Int, Int> {
        val wins = autoTradeLogDao.getWinCount()
        val total = autoTradeLogDao.getTotalTradeCount()
        return Pair(wins, total)
    }
    
    suspend fun deleteAutoTradeLog(id: Long) {
        autoTradeLogDao.deleteAutoTradeLog(id)
    }
    
    suspend fun exportToCsv(): String {
        val logs = autoTradeLogDao.getAutoTradeLogsForDate("1970-01-01") // Get all
        
        val csvBuilder = StringBuilder()
        csvBuilder.append("date,symbol,entry_price,exit_price,target_price,stop_price,quantity,")
        csvBuilder.append("entry_time,exit_time,profit_loss,profit_loss_percent,exit_reason,signal_type\n")
        
        logs.forEach { log ->
            csvBuilder.append("${log.date},${log.symbol},${log.entryPrice},${log.exitPrice},")
            csvBuilder.append("${log.targetPrice},${log.stopPrice},${log.quantity},")
            csvBuilder.append("${log.entryTime},${log.exitTime},${log.profitLoss},")
            csvBuilder.append("${log.profitLossPercent},${log.exitReason},${log.signalType}\n")
        }
        
        return csvBuilder.toString()
    }
}
