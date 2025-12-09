package com.alpaca.traderpro.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<DailyLogEntity>>
    
    @Query("SELECT * FROM daily_logs WHERE symbol = :symbol ORDER BY date DESC")
    fun getLogsBySymbol(symbol: String): Flow<List<DailyLogEntity>>
    
    @Query("SELECT * FROM daily_logs WHERE date >= :startDate ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentLogs(startDate: String, limit: Int): List<DailyLogEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLogEntity)
    
    @Query("DELETE FROM daily_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)
    
    @Query("DELETE FROM daily_logs")
    suspend fun deleteAllLogs()
}
