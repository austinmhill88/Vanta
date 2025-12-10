package com.alpaca.traderpro.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.dp
import com.alpaca.traderpro.data.model.Position
import com.alpaca.traderpro.ui.theme.*
import java.text.NumberFormat
import java.util.*

data class PortfolioUiState(
    val positions: List<Position> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalValue: Double = 0.0,
    val totalPL: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    uiState: PortfolioUiState,
    onRefresh: () -> Unit,
    onPositionClick: (String) -> Unit,
    onClosePosition: (String) -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Portfolio Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Portfolio Summary",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Value",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = currencyFormatter.format(uiState.totalValue),
                            style = MaterialTheme.typography.headlineMedium,
                            color = LightGreen
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total P/L",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = currencyFormatter.format(uiState.totalPL),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (uiState.totalPL >= 0) LightGreen else LightRed
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Positions Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Open Positions (${uiState.positions.size})",
                style = MaterialTheme.typography.titleMedium
            )
            
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Positions List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.positions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No open positions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.positions) { position ->
                    PositionCard(
                        position = position,
                        currencyFormatter = currencyFormatter,
                        onClick = { onPositionClick(position.symbol) },
                        onClose = { onClosePosition(position.symbol) }
                    )
                }
            }
        }
        
        // Error Message
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun PositionCard(
    position: Position,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val unrealizedPl = position.unrealizedPl.toDoubleOrNull() ?: 0.0
    val unrealizedPlpc = position.unrealizedPlpc.toDoubleOrNull() ?: 0.0
    val currentPrice = position.currentPrice.toDoubleOrNull() ?: 0.0
    val avgEntryPrice = position.avgEntryPrice.toDoubleOrNull() ?: 0.0
    val marketValue = position.marketValue.toDoubleOrNull() ?: 0.0
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = position.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = LightGreen
                    )
                    Text(
                        text = "${position.qty} shares",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                </Column>
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Position",
                        tint = LightRed
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
                        text = "Current Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(currentPrice),
                        style = MaterialTheme.typography.titleMedium
                    )
                </Column>
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Avg Entry",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(avgEntryPrice),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Market Value",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(marketValue),
                        style = MaterialTheme.typography.titleMedium
                    )
                </Column>
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Unrealized P/L",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = currencyFormatter.format(unrealizedPl),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (unrealizedPl >= 0) LightGreen else LightRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("(%.2f%%)", unrealizedPlpc),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (unrealizedPl >= 0) LightGreen else LightRed
                        )
                    }
                }
            }
        }
    }
}
