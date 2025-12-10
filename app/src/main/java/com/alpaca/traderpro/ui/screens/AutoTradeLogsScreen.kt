package com.alpaca.traderpro.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.alpaca.traderpro.data.database.AutoTradeLogEntity
import com.alpaca.traderpro.domain.AutoTradeLogsUiState
import com.alpaca.traderpro.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTradeLogsScreen(
    uiState: AutoTradeLogsUiState,
    onSearchQueryChange: (String) -> Unit,
    onExportClick: () -> Unit,
    onDeleteLog: (Long) -> Unit
) {
    val context = LocalContext.current
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    // Handle export success
    LaunchedEffect(uiState.exportSuccess, uiState.csvFile) {
        if (uiState.exportSuccess && uiState.csvFile != null) {
            shareCsvFile(context, uiState.csvFile)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Win Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "${uiState.winRate}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (uiState.winRate >= 50) LightGreen else LightRed
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Trades",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "${uiState.totalTrades}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentBlue
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Today P/L",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(uiState.todayProfitLoss),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (uiState.todayProfitLoss >= 0) LightGreen else LightRed
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search auto-trade logs...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Export Button
        Button(
            onClick = onExportClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export CSV")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Logs List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoMode,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No auto-trades yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredLogs, key = { it.id }) { log ->
                    AutoTradeLogItem(
                        log = log,
                        currencyFormatter = currencyFormatter,
                        onDelete = { onDeleteLog(log.id) }
                    )
                }
            }
        }
        
        // Error Message
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun AutoTradeLogItem(
    log: AutoTradeLogEntity,
    currencyFormatter: NumberFormat,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val isProfitable = log.profitLoss > 0
    val exitReasonColor = when (log.exitReason) {
        "TARGET" -> LightGreen
        "STOP" -> LightRed
        "FORCE_EXIT" -> AccentBlue
        else -> OnSurfaceVariant
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isProfitable) DarkGreen.copy(alpha = 0.3f) 
                            else DarkRed.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "SHORT",
                            tint = AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            color = AccentBlue
                        )
                    }
                    Text(
                        text = "${log.date} • ${log.entryTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(log.profitLoss),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isProfitable) LightGreen else LightRed
                    )
                    Text(
                        text = String.format("%.2f%%", log.profitLossPercent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isProfitable) LightGreen else LightRed
                    )
                }
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Trade Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Entry",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = currencyFormatter.format(log.entryPrice),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Target",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightGreen
                            )
                            Text(
                                text = currencyFormatter.format(log.targetPrice),
                                style = MaterialTheme.typography.bodyLarge,
                                color = LightGreen
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                        )
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Stop",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightRed
                            )
                            Text(
                                text = currencyFormatter.format(log.stopPrice),
                                style = MaterialTheme.typography.bodyLarge,
                                color = LightRed
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Exit",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = currencyFormatter.format(log.exitPrice),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "at ${log.exitTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Quantity",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "${log.quantity} shares",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Exit Reason Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = exitReasonColor.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (log.exitReason) {
                                    "TARGET" -> Icons.Default.CheckCircle
                                    "STOP" -> Icons.Default.Cancel
                                    "FORCE_EXIT" -> Icons.Default.AccessTime
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = exitReasonColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (log.exitReason) {
                                    "TARGET" -> "Target Hit ✓"
                                    "STOP" -> "Stopped Out"
                                    "FORCE_EXIT" -> "Force Exit"
                                    else -> "Manual Exit"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = exitReasonColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AccentRed
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

private fun shareCsvFile(context: Context, file: java.io.File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    
    context.startActivity(Intent.createChooser(shareIntent, "Share CSV"))
}
