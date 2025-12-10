package com.alpaca.traderpro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyLogEntity::class, AutoTradeLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AlpacaDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun autoTradeLogDao(): AutoTradeLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AlpacaDatabase? = null
        
        fun getDatabase(context: Context): AlpacaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlpacaDatabase::class.java,
                    "alpaca_trader_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
