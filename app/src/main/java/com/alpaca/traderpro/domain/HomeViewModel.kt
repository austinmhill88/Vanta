package com.alpaca.traderpro.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpaca.traderpro.data.model.TradeUpdate
import com.alpaca.traderpro.data.repository.AlpacaRepository
import com.alpaca.traderpro.data.repository.LogsRepository
import com.alpaca.traderpro.data.repository.SecurePreferencesManager
import com.alpaca.traderpro.data.service.WebSocketService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime

data class HomeUiState(
    val symbol: String = "AAPL",
    val currentPrice: Double = 0.0,
    val todayHigh: Double = 0.0,
    val todayHighTime: LocalTime? = null,
    val todayLow: Double = Double.MAX_VALUE,
    val todayLowTime: LocalTime? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val profitLoss: Double? = null,
    val buyingPower: Double = 0.0,
    val priceHistory: List<Pair<Long, Double>> = emptyList(),
    val showBuyConfirmation: Boolean = false,
    val showSellConfirmation: Boolean = false,
    val tradeSuccess: Boolean = false,
    val showCelebration: Boolean = false,
    val celebrationType: CelebrationType = CelebrationType.NONE
)

enum class CelebrationType {
    NONE, PROFIT, BIG_WIN
}

class HomeViewModel(
    private val alpacaRepository: AlpacaRepository,
    private val logsRepository: LogsRepository,
    private val preferencesManager: SecurePreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var webSocketService: WebSocketService? = null
    private var priceStreamJob: Job? = null
    
    init {
        loadAccount()
        connectToLivePrices()
    }
    
    fun updateSymbol(symbol: String) {
        _uiState.update { it.copy(symbol = symbol.uppercase()) }
        reconnectWebSocket()
    }
    
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadAccount()
        reconnectWebSocket()
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
    
    private fun loadAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            alpacaRepository.getAccount().fold(
                onSuccess = { account ->
                    _uiState.update { 
                        it.copy(
                            buyingPower = account.buyingPower.toDoubleOrNull() ?: 0.0,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
            )
        }
    }
    
    private fun connectToLivePrices() {
        val apiKey = preferencesManager.getApiKey() ?: return
        val apiSecret = preferencesManager.getApiSecret() ?: return
        
        webSocketService = WebSocketService(apiKey, apiSecret)
        
        priceStreamJob = viewModelScope.launch {
            webSocketService?.connectToLivePrices(_uiState.value.symbol)
                ?.collect { tradeUpdate ->
                    updatePrice(tradeUpdate)
                }
        }
    }
    
    private fun reconnectWebSocket() {
        priceStreamJob?.cancel()
        webSocketService?.disconnect()
        connectToLivePrices()
    }
    
    private fun updatePrice(tradeUpdate: TradeUpdate) {
        val currentState = _uiState.value
        val newPrice = tradeUpdate.price
        val currentTime = LocalTime.now()
        
        val newHigh = maxOf(currentState.todayHigh, newPrice)
        val newLow = if (currentState.todayLow == Double.MAX_VALUE) {
            newPrice
        } else {
            minOf(currentState.todayLow, newPrice)
        }
        
        val priceHistory = (currentState.priceHistory + (System.currentTimeMillis() to newPrice))
            .takeLast(60) // Keep last 60 data points
        
        _uiState.update {
            it.copy(
                currentPrice = newPrice,
                todayHigh = newHigh,
                todayHighTime = if (newHigh == newPrice) currentTime else it.todayHighTime,
                todayLow = newLow,
                todayLowTime = if (newLow == newPrice) currentTime else it.todayLowTime,
                priceHistory = priceHistory
            )
        }
    }
    
    fun showBuyConfirmation() {
        _uiState.update { it.copy(showBuyConfirmation = true) }
    }
    
    fun hideBuyConfirmation() {
        _uiState.update { it.copy(showBuyConfirmation = false) }
    }
    
    fun showSellConfirmation() {
        _uiState.update { it.copy(showSellConfirmation = true) }
    }
    
    fun hideSellConfirmation() {
        _uiState.update { it.copy(showSellConfirmation = false) }
    }
    
    fun executeBuy() {
        viewModelScope.launch {
            hideBuyConfirmation()
            _uiState.update { it.copy(isLoading = true) }
            
            val currentState = _uiState.value
            alpacaRepository.buyWithLeverage(
                currentState.symbol,
                currentState.buyingPower,
                currentState.currentPrice
            ).fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            tradeSuccess = true,
                            errorMessage = null
                        )
                    }
                    loadAccount()
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
            )
        }
    }
    
    fun executeSell() {
        viewModelScope.launch {
            hideSellConfirmation()
            _uiState.update { it.copy(isLoading = true) }
            
            val currentState = _uiState.value
            
            // Get position to calculate P&L
            alpacaRepository.getPosition(currentState.symbol).fold(
                onSuccess = { position ->
                    val unrealizedPl = position.unrealizedPl.toDoubleOrNull() ?: 0.0
                    
                    alpacaRepository.sellAll(currentState.symbol).fold(
                        onSuccess = {
                            // Save daily log
                            if (currentState.todayHighTime != null && currentState.todayLowTime != null) {
                                viewModelScope.launch {
                                    logsRepository.saveDailyLog(
                                        currentState.symbol,
                                        currentState.todayHigh,
                                        currentState.todayHighTime,
                                        currentState.todayLow,
                                        currentState.todayLowTime
                                    )
                                }
                            }
                            
                            val celebration = when {
                                unrealizedPl > currentState.buyingPower * 0.01 -> CelebrationType.BIG_WIN
                                unrealizedPl > 0 -> CelebrationType.PROFIT
                                else -> CelebrationType.NONE
                            }
                            
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    tradeSuccess = true,
                                    profitLoss = unrealizedPl,
                                    showCelebration = celebration != CelebrationType.NONE,
                                    celebrationType = celebration,
                                    errorMessage = null
                                )
                            }
                            loadAccount()
                        },
                        onFailure = { exception ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    errorMessage = exception.message
                                )
                            }
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
            )
        }
    }
    
    fun dismissCelebration() {
        _uiState.update { 
            it.copy(
                showCelebration = false,
                celebrationType = CelebrationType.NONE,
                tradeSuccess = false
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketService?.disconnect()
        priceStreamJob?.cancel()
    }
}
