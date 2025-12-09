package com.alpaca.traderpro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AlpacaDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AlpacaDatabase? = null
        
        fun getDatabase(context: Context): AlpacaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlpacaDatabase::class.java,
                    "alpaca_trader_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
