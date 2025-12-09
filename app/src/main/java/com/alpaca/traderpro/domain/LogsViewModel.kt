package com.alpaca.traderpro.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpaca.traderpro.data.database.DailyLogEntity
import com.alpaca.traderpro.data.repository.LogsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class LogsUiState(
    val logs: List<DailyLogEntity> = emptyList(),
    val filteredLogs: List<DailyLogEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val exportSuccess: Boolean = false,
    val csvFile: File? = null
)

class LogsViewModel(
    private val logsRepository: LogsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()
    
    init {
        loadLogs()
    }
    
    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            logsRepository.getAllLogs()
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
    
    fun updateSearchQuery(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredLogs = filterLogs(it.logs, query)
            )
        }
    }
    
    private fun filterLogs(logs: List<DailyLogEntity>, query: String): List<DailyLogEntity> {
        if (query.isBlank()) return logs
        
        return logs.filter { log ->
            log.symbol.contains(query, ignoreCase = true) ||
            log.date.contains(query, ignoreCase = true)
        }
    }
    
    fun exportToCsv(filesDir: File) {
        viewModelScope.launch {
            try {
                val csvContent = logsRepository.exportToCsv()
                
                val csvDir = File(filesDir, "csv")
                if (!csvDir.exists()) {
                    csvDir.mkdirs()
                }
                
                val csvFile = File(csvDir, "alpaca_logs_${System.currentTimeMillis()}.csv")
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
            logsRepository.deleteLog(id)
        }
    }
    
    fun clearExportSuccess() {
        _uiState.update { it.copy(exportSuccess = false, csvFile = null) }
    }
}
