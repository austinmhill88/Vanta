package com.alpaca.traderpro

import android.app.Application
import com.alpaca.traderpro.data.database.AlpacaDatabase

class AlpacaTraderApp : Application() {
    
    val database: AlpacaDatabase by lazy {
        AlpacaDatabase.getDatabase(this)
    }
    
    override fun onCreate() {
        super.onCreate()
    }
}
