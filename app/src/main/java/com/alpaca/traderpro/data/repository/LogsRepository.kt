package com.alpaca.traderpro.data.repository

import com.alpaca.traderpro.data.database.DailyLogDao
import com.alpaca.traderpro.data.database.DailyLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LogsRepository(
    private val dailyLogDao: DailyLogDao
) {
    
    fun getAllLogs(): Flow<List<DailyLogEntity>> {
        return dailyLogDao.getAllLogs()
    }
    
    fun getLogsBySymbol(symbol: String): Flow<List<DailyLogEntity>> {
        return dailyLogDao.getLogsBySymbol(symbol)
    }
    
    suspend fun saveDailyLog(
        symbol: String,
        highPrice: Double,
        highTime: LocalTime,
        lowPrice: Double,
        lowTime: LocalTime
    ) {
        val diffDollar = highPrice - lowPrice
        val diffPercent = (diffDollar / lowPrice) * 100
        
        val log = DailyLogEntity(
            date = LocalDate.now().toString(),
            symbol = symbol,
            highPrice = highPrice,
            highTime = highTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            lowPrice = lowPrice,
            lowTime = lowTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            diffDollar = diffDollar,
            diffPercent = diffPercent
        )
        
        dailyLogDao.insertLog(log)
    }
    
    suspend fun getRecentLogsForAnalysis(limit: Int = 30): List<DailyLogEntity> {
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        return dailyLogDao.getRecentLogs(thirtyDaysAgo, limit)
    }
    
    suspend fun deleteLog(id: Long) {
        dailyLogDao.deleteLog(id)
    }
    
    suspend fun deleteAllLogs() {
        dailyLogDao.deleteAllLogs()
    }
    
    suspend fun exportToCsv(): String {
        val logs = dailyLogDao.getRecentLogs("1970-01-01", Int.MAX_VALUE)
        
        val csvBuilder = StringBuilder()
        csvBuilder.append("date,symbol,high,high_time,low,low_time,diff_dollar,diff_percent\n")
        
        logs.forEach { log ->
            csvBuilder.append("${log.date},${log.symbol},${log.highPrice},${log.highTime},")
            csvBuilder.append("${log.lowPrice},${log.lowTime},${log.diffDollar},${log.diffPercent}\n")
        }
        
        return csvBuilder.toString()
    }
}
