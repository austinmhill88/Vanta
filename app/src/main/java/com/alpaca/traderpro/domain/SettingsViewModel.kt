package com.alpaca.traderpro.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpaca.traderpro.data.repository.LogsRepository
import com.alpaca.traderpro.data.repository.SecurePreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class SettingsUiState(
    val apiKey: String = "",
    val apiSecret: String = "",
    val buyWindowStart: String = "",
    val buyWindowEnd: String = "",
    val sellWindowStart: String = "",
    val sellWindowEnd: String = "",
    val suggestedBuyWindow: String = "",
    val suggestedSellWindow: String = "",
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val preferencesManager: SecurePreferencesManager,
    private val logsRepository: LogsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        _uiState.update {
            it.copy(
                apiKey = preferencesManager.getApiKey() ?: "",
                apiSecret = preferencesManager.getApiSecret() ?: "",
                buyWindowStart = preferencesManager.getBuyWindowStart() ?: "",
                buyWindowEnd = preferencesManager.getBuyWindowEnd() ?: "",
                sellWindowStart = preferencesManager.getSellWindowStart() ?: "",
                sellWindowEnd = preferencesManager.getSellWindowEnd() ?: "",
                notificationsEnabled = preferencesManager.areNotificationsEnabled()
            )
        }
    }
    
    fun updateApiKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
    }
    
    fun updateApiSecret(apiSecret: String) {
        _uiState.update { it.copy(apiSecret = apiSecret) }
    }
    
    fun updateBuyWindowStart(time: String) {
        _uiState.update { it.copy(buyWindowStart = time) }
    }
    
    fun updateBuyWindowEnd(time: String) {
        _uiState.update { it.copy(buyWindowEnd = time) }
    }
    
    fun updateSellWindowStart(time: String) {
        _uiState.update { it.copy(sellWindowStart = time) }
    }
    
    fun updateSellWindowEnd(time: String) {
        _uiState.update { it.copy(sellWindowEnd = time) }
    }
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }
    
    fun suggestTimeWindows() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val recentLogs = logsRepository.getRecentLogsForAnalysis(30)
                
                if (recentLogs.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Not enough historical data for suggestions"
                        )
                    }
                    return@launch
                }
                
                // Calculate average low times for buy window
                val lowTimes = recentLogs.mapNotNull { log ->
                    try {
                        LocalTime.parse(log.lowTime, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        null
                    }
                }
                
                // Calculate average high times for sell window
                val highTimes = recentLogs.mapNotNull { log ->
                    try {
                        LocalTime.parse(log.highTime, DateTimeFormatter.ofPattern("HH:mm"))
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (lowTimes.isNotEmpty() && highTimes.isNotEmpty()) {
                    val avgLowMinutes = lowTimes.map { it.hour * 60 + it.minute }.average().toInt()
                    val avgHighMinutes = highTimes.map { it.hour * 60 + it.minute }.average().toInt()
                    
                    val buyStart = LocalTime.of(avgLowMinutes / 60, avgLowMinutes % 60).minusMinutes(30)
                    val buyEnd = LocalTime.of(avgLowMinutes / 60, avgLowMinutes % 60).plusMinutes(30)
                    
                    val sellStart = LocalTime.of(avgHighMinutes / 60, avgHighMinutes % 60).minusMinutes(30)
                    val sellEnd = LocalTime.of(avgHighMinutes / 60, avgHighMinutes % 60).plusMinutes(30)
                    
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            suggestedBuyWindow = "${buyStart.format(formatter)} - ${buyEnd.format(formatter)} ET",
                            suggestedSellWindow = "${sellStart.format(formatter)} - ${sellEnd.format(formatter)} ET",
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not parse time data"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentState = _uiState.value
                
                preferencesManager.saveApiKey(currentState.apiKey)
                preferencesManager.saveApiSecret(currentState.apiSecret)
                preferencesManager.saveBuyWindowStart(currentState.buyWindowStart)
                preferencesManager.saveBuyWindowEnd(currentState.buyWindowEnd)
                preferencesManager.saveSellWindowStart(currentState.sellWindowStart)
                preferencesManager.saveSellWindowEnd(currentState.sellWindowEnd)
                preferencesManager.saveNotificationsEnabled(currentState.notificationsEnabled)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        errorMessage = null
                    )
                }
                
                // Reset success after a delay
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(saveSuccess = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}
