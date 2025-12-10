package com.alpaca.traderpro.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpaca.traderpro.data.database.AutoTradeLogEntity
import com.alpaca.traderpro.data.repository.AutoTradeLogsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class AutoTradeLogsUiState(
    val logs: List<AutoTradeLogEntity> = emptyList(),
    val filteredLogs: List<AutoTradeLogEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val exportSuccess: Boolean = false,
    val csvFile: File? = null,
    val winRate: Int = 0,
    val totalTrades: Int = 0,
    val todayProfitLoss: Double = 0.0
)

class AutoTradeLogsViewModel(
    private val autoTradeLogsRepository: AutoTradeLogsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AutoTradeLogsUiState())
    val uiState: StateFlow<AutoTradeLogsUiState> = _uiState.asStateFlow()
    
    init {
        loadLogs()
        loadStats()
    }
    
    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            autoTradeLogsRepository.getAllAutoTradeLogs()
                .catch { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
                .collect { logs ->
                    _uiState.update { 
                        it.copy(
                            logs = logs,
                            filteredLogs = filterLogs(logs, it.searchQuery),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            val (wins, total) = autoTradeLogsRepository.getWinRate()
            val winRate = if (total > 0) (wins * 100) / total else 0
            val todayPL = autoTradeLogsRepository.getTodayTotalProfitLoss()
            
            _uiState.update {
                it.copy(
                    winRate = winRate,
                    totalTrades = total,
                    todayProfitLoss = todayPL
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredLogs = filterLogs(it.logs, query)
            )
        }
    }
    
    private fun filterLogs(logs: List<AutoTradeLogEntity>, query: String): List<AutoTradeLogEntity> {
        if (query.isBlank()) return logs
        
        return logs.filter { log ->
            log.symbol.contains(query, ignoreCase = true) ||
            log.date.contains(query, ignoreCase = true)
        }
    }
    
    fun exportToCsv(filesDir: File) {
        viewModelScope.launch {
            try {
                val csvContent = autoTradeLogsRepository.exportToCsv()
                
                val csvDir = File(filesDir, "csv")
                if (!csvDir.exists()) {
                    csvDir.mkdirs()
                }
                
                val csvFile = File(csvDir, "auto_trade_logs_${System.currentTimeMillis()}.csv")
                csvFile.writeText(csvContent)
                
                _uiState.update { 
                    it.copy(
                        exportSuccess = true,
                        csvFile = csvFile
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Export failed: ${e.message}")
                }
            }
        }
    }
    
    fun deleteLog(id: Long) {
        viewModelScope.launch {
            autoTradeLogsRepository.deleteAutoTradeLog(id)
            loadStats()
        }
    }
    
    fun clearExportSuccess() {
        _uiState.update { it.copy(exportSuccess = false, csvFile = null) }
    }
}
