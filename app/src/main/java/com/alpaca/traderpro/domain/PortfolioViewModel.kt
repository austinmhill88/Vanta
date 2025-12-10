package com.alpaca.traderpro.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpaca.traderpro.data.repository.AlpacaRepository
import com.alpaca.traderpro.ui.screens.PortfolioUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val alpacaRepository: AlpacaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()
    
    init {
        loadPositions()
    }
    
    fun refresh() {
        loadPositions()
    }
    
    fun loadPositions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            alpacaRepository.getAllPositions().fold(
                onSuccess = { positions ->
                    val totalValue = positions.sumOf { 
                        it.marketValue.toDoubleOrNull() ?: 0.0 
                    }
                    val totalPL = positions.sumOf { 
                        it.unrealizedPl.toDoubleOrNull() ?: 0.0 
                    }
                    
                    _uiState.update { 
                        it.copy(
                            positions = positions,
                            totalValue = totalValue,
                            totalPL = totalPL,
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
    
    fun closePosition(symbol: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            alpacaRepository.sellAll(symbol).fold(
                onSuccess = {
                    // Reload positions after closing
                    loadPositions()
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
}
