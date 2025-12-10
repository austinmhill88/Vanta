package com.alpaca.traderpro.data.service

import android.util.Log
import com.alpaca.traderpro.data.model.*
import com.alpaca.traderpro.data.repository.SecurePreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.ZoneId

class AutoTrader(
    private val preferencesManager: SecurePreferencesManager
) {
    private val _currentSignal = MutableStateFlow<AutoTradeSignal?>(null)
    val currentSignal: StateFlow<AutoTradeSignal?> = _currentSignal.asStateFlow()
    
    private val _liveTrade = MutableStateFlow<LiveTrade?>(null)
    val liveTrade: StateFlow<LiveTrade?> = _liveTrade.asStateFlow()
    
    private val candleBuffer = mutableListOf<Candle>()
    
    fun updateCandle(candle: Candle) {
        candleBuffer.add(candle)
        // Keep only last 10 candles
        if (candleBuffer.size > 10) {
            candleBuffer.removeAt(0)
        }
    }
    
    fun evaluateShortSignal(
        symbol: String,
        currentPrice: Double,
        vwap: Double
    ): AutoTradeSignal? {
        if (!preferencesManager.isAutoModeEnabled()) return null
        
        val now = LocalTime.now(ZoneId.of("America/New_York"))
        
        // 1. Must be inside buy window
        val buyWindowStart = parseTime(preferencesManager.getBuyWindowStart() ?: "12:45")
        val buyWindowEnd = parseTime(preferencesManager.getBuyWindowEnd() ?: "13:45")
        
        if (!now.isInRange(buyWindowStart, buyWindowEnd)) {
            Log.d("AutoTrader", "Outside buy window")
            return null
        }
        
        // 2. Price below VWAP or yesterday close (downtrend filter)
        if (preferencesManager.useVWAPFilter() && currentPrice > vwap) {
            Log.d("AutoTrader", "Price above VWAP")
            return null
        }
        
        // 3. Last 5-min candle = lower high + red close
        if (candleBuffer.size < 2) {
            Log.d("AutoTrader", "Not enough candles")
            return null
        }
        
        val recent = candleBuffer.takeLast(2)
        val previousCandle = recent[0]
        val currentCandle = recent[1]
        
        val isLowerHigh = currentCandle.high < previousCandle.high
        val isRedClose = currentCandle.close < currentCandle.open
        
        if (!isLowerHigh || !isRedClose) {
            Log.d("AutoTrader", "Pattern not matched: lowerHigh=$isLowerHigh, redClose=$isRedClose")
            return null
        }
        
        // Calculate target and stop prices for SHORT
        val targetPercent = preferencesManager.getTargetPercent()
        val stopPercent = preferencesManager.getStopPercent()
        
        val targetPrice = currentPrice * (1 - targetPercent / 100)
        val stopPrice = currentPrice * (1 + stopPercent / 100)
        
        val signal = AutoTradeSignal(
            symbol = symbol,
            signalTime = now.toString(),
            entryPrice = currentPrice,
            targetPrice = targetPrice,
            stopPrice = stopPrice,
            signalType = "SHORT"
        )
        
        _currentSignal.value = signal
        Log.d("AutoTrader", "SHORT SIGNAL generated: $signal")
        
        return signal
    }
    
    fun setLiveTrade(trade: LiveTrade) {
        _liveTrade.value = trade
    }
    
    fun clearLiveTrade() {
        _liveTrade.value = null
    }
    
    fun clearSignal() {
        _currentSignal.value = null
    }
    
    private fun parseTime(time: String): LocalTime {
        return try {
            val parts = time.split(":")
            LocalTime.of(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            LocalTime.of(12, 45) // Default
        }
    }
    
    private fun LocalTime.isInRange(start: LocalTime, end: LocalTime): Boolean {
        return this.isAfter(start) && this.isBefore(end)
    }
}
