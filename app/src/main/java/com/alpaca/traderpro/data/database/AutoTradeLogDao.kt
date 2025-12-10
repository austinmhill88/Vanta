package com.alpaca.traderpro.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoTradeLogDao {
    @Query("SELECT * FROM auto_trade_logs ORDER BY date DESC, entryTime DESC")
    fun getAllAutoTradeLogs(): Flow<List<AutoTradeLogEntity>>
    
    @Query("SELECT * FROM auto_trade_logs WHERE symbol = :symbol ORDER BY date DESC")
    fun getAutoTradeLogsBySymbol(symbol: String): Flow<List<AutoTradeLogEntity>>
    
    @Query("SELECT * FROM auto_trade_logs WHERE date = :date ORDER BY entryTime DESC")
    suspend fun getAutoTradeLogsForDate(date: String): List<AutoTradeLogEntity>
    
    @Query("SELECT COUNT(*) FROM auto_trade_logs WHERE date = :date")
    suspend fun getAutoTradeCountForDate(date: String): Int
    
    @Query("SELECT SUM(profitLoss) FROM auto_trade_logs WHERE date = :date")
    suspend fun getTotalProfitLossForDate(date: String): Double?
    
    @Query("SELECT COUNT(*) FROM auto_trade_logs WHERE profitLoss > 0")
    suspend fun getWinCount(): Int
    
    @Query("SELECT COUNT(*) FROM auto_trade_logs")
    suspend fun getTotalTradeCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoTradeLog(log: AutoTradeLogEntity)
    
    @Query("DELETE FROM auto_trade_logs WHERE id = :id")
    suspend fun deleteAutoTradeLog(id: Long)
    
    @Query("DELETE FROM auto_trade_logs")
    suspend fun deleteAllAutoTradeLogs()
}
