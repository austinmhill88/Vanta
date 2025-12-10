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
    val errorMessage: String? = null,
    // Auto-Mode Settings
    val autoModeEnabled: Boolean = false,
    val sellByTime: String = "14:30",
    val targetPercent: Float = 0.32f,
    val stopPercent: Float = 0.25f,
    val useVWAPFilter: Boolean = true,
    val showAutoModeConfirmation: Boolean = false
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
                buyWindowStart = preferencesManager.getBuyWindowStart() ?: "12:45",
                buyWindowEnd = preferencesManager.getBuyWindowEnd() ?: "13:45",
                sellWindowStart = preferencesManager.getSellWindowStart() ?: "",
                sellWindowEnd = preferencesManager.getSellWindowEnd() ?: "",
                notificationsEnabled = preferencesManager.areNotificationsEnabled(),
                autoModeEnabled = preferencesManager.isAutoModeEnabled(),
                sellByTime = preferencesManager.getSellByTime(),
                targetPercent = preferencesManager.getTargetPercent(),
                stopPercent = preferencesManager.getStopPercent(),
                useVWAPFilter = preferencesManager.useVWAPFilter()
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
    
    fun updateAutoModeEnabled(enabled: Boolean) {
        if (enabled && !preferencesManager.isAutoModeConfirmed()) {
            _uiState.update { it.copy(showAutoModeConfirmation = true) }
        } else {
            _uiState.update { it.copy(autoModeEnabled = enabled) }
        }
    }
    
    fun confirmAutoMode() {
        preferencesManager.saveAutoModeConfirmed(true)
        _uiState.update { 
            it.copy(
                autoModeEnabled = true,
                showAutoModeConfirmation = false
            )
        }
    }
    
    fun cancelAutoMode() {
        _uiState.update { 
            it.copy(
                autoModeEnabled = false,
                showAutoModeConfirmation = false
            )
        }
    }
    
    fun updateSellByTime(time: String) {
        _uiState.update { it.copy(sellByTime = time) }
    }
    
    fun updateTargetPercent(percent: Float) {
        _uiState.update { it.copy(targetPercent = percent) }
    }
    
    fun updateStopPercent(percent: Float) {
        _uiState.update { it.copy(stopPercent = percent) }
    }
    
    fun updateUseVWAPFilter(enabled: Boolean) {
        _uiState.update { it.copy(useVWAPFilter = enabled) }
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
                preferencesManager.saveAutoModeEnabled(currentState.autoModeEnabled)
                preferencesManager.saveSellByTime(currentState.sellByTime)
                preferencesManager.saveTargetPercent(currentState.targetPercent)
                preferencesManager.saveStopPercent(currentState.stopPercent)
                preferencesManager.saveUseVWAPFilter(currentState.useVWAPFilter)
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
