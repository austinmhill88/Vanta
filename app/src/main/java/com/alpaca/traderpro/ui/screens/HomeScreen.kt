package com.alpaca.traderpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.alpaca.traderpro.R
import com.alpaca.traderpro.domain.CelebrationType
import com.alpaca.traderpro.domain.HomeUiState
import com.alpaca.traderpro.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSymbolChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit,
    onConfirmBuy: () -> Unit,
    onConfirmSell: () -> Unit,
    onDismissBuyConfirmation: () -> Unit,
    onDismissSellConfirmation: () -> Unit,
    onDismissCelebration: () -> Unit,
    onToggleAdvancedTrading: () -> Unit,
    onCustomQuantityChange: (String) -> Unit,
    onOrderTypeChange: (com.alpaca.traderpro.domain.OrderType) -> Unit,
    onLimitPriceChange: (String) -> Unit,
    onStopPriceChange: (String) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Price animation
    val priceScale by animateFloatAsState(
        targetValue = if (uiState.currentPrice > 0) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (pullToRefreshState.isRefreshing) {
            LaunchedEffect(true) {
                onRefresh()
            }
        }
        
        LaunchedEffect(uiState.isRefreshing) {
            if (!uiState.isRefreshing) {
                pullToRefreshState.endRefresh()
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Symbol Input with Search Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.symbol,
                    onValueChange = onSymbolChange,
                    label = { Text("Symbol") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = {
                            onRefresh()
                        }
                    )
                )
                
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Symbol",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Portfolio Overview Card
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
                        text = "Portfolio Overview",
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
                                text = "Portfolio Value",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = currencyFormatter.format(uiState.portfolioValue),
                                style = MaterialTheme.typography.titleMedium,
                                color = LightGreen
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Cash Available",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = currencyFormatter.format(uiState.cash),
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
                                text = "Day's P/L",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = currencyFormatter.format(uiState.dayProfitLoss),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (uiState.dayProfitLoss >= 0) LightGreen else LightRed
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Buying Power",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = currencyFormatter.format(uiState.buyingPower),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    // Positions Summary
                    if (uiState.positions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Open Positions (${uiState.positions.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        uiState.positions.take(3).forEach { position ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = position.symbol,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Row {
                                    Text(
                                        text = "${position.qty} @ ${currencyFormatter.format(position.avgEntryPrice.toDoubleOrNull() ?: 0.0)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currencyFormatter.format(position.unrealizedPl.toDoubleOrNull() ?: 0.0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if ((position.unrealizedPl.toDoubleOrNull() ?: 0.0) >= 0) LightGreen else LightRed
                                    )
                                }
                            }
                        }
                        
                        if (uiState.positions.size > 3) {
                            Text(
                                text = "+ ${uiState.positions.size - 3} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Current Price Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(priceScale),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Price",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = currencyFormatter.format(uiState.currentPrice),
                        style = MaterialTheme.typography.displayLarge,
                        color = LightGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Today's High/Low
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "High",
                            tint = LightGreen
                        )
                        Text(
                            text = "High",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = currencyFormatter.format(uiState.todayHigh),
                            style = MaterialTheme.typography.titleLarge
                        )
                        uiState.todayHighTime?.let {
                            Text(
                                text = it.toString(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Low",
                            tint = LightRed
                        )
                        Text(
                            text = "Low",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = if (uiState.todayLow == Double.MAX_VALUE) "$0.00" 
                                   else currencyFormatter.format(uiState.todayLow),
                            style = MaterialTheme.typography.titleLarge
                        )
                        uiState.todayLowTime?.let {
                            Text(
                                text = it.toString(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Trading Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onBuyClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.showAdvancedTrading && uiState.customQuantity.isNotBlank()) 
                            "Buy ${uiState.customQuantity}" 
                        else 
                            "Buy All (2x)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = onSellClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Sell,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.showAdvancedTrading && uiState.customQuantity.isNotBlank()) 
                            "Sell ${uiState.customQuantity}" 
                        else 
                            "Sell All",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Advanced Trading Toggle
            TextButton(
                onClick = onToggleAdvancedTrading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (uiState.showAdvancedTrading) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.showAdvancedTrading) "Hide Advanced Trading" else "Show Advanced Trading"
                )
            }
            
            // Advanced Trading Options
            AnimatedVisibility(
                visible = uiState.showAdvancedTrading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceDark
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Advanced Trading",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Quantity Input
                        OutlinedTextField(
                            value = uiState.customQuantity,
                            onValueChange = onCustomQuantityChange,
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Order Type Selector
                        Text(
                            text = "Order Type",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.alpaca.traderpro.domain.OrderType.values().forEach { orderType ->
                                FilterChip(
                                    selected = uiState.orderType == orderType,
                                    onClick = { onOrderTypeChange(orderType) },
                                    label = { Text(orderType.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Limit Price (for LIMIT and STOP_LIMIT orders)
                        if (uiState.orderType == com.alpaca.traderpro.domain.OrderType.LIMIT || 
                            uiState.orderType == com.alpaca.traderpro.domain.OrderType.STOP_LIMIT) {
                            OutlinedTextField(
                                value = uiState.limitPrice,
                                onValueChange = onLimitPriceChange,
                                label = { Text("Limit Price") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                prefix = { Text("$") }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Stop Price (for STOP and STOP_LIMIT orders)
                        if (uiState.orderType == com.alpaca.traderpro.domain.OrderType.STOP || 
                            uiState.orderType == com.alpaca.traderpro.domain.OrderType.STOP_LIMIT) {
                            OutlinedTextField(
                                value = uiState.stopPrice,
                                onValueChange = onStopPriceChange,
                                label = { Text("Stop Price") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                                ),
                                prefix = { Text("$") }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Info text
                        Text(
                            text = when (uiState.orderType) {
                                com.alpaca.traderpro.domain.OrderType.MARKET -> "Market orders execute immediately at the current market price."
                                com.alpaca.traderpro.domain.OrderType.LIMIT -> "Limit orders execute only at the specified price or better."
                                com.alpaca.traderpro.domain.OrderType.STOP -> "Stop orders trigger a market order when the stop price is reached."
                                com.alpaca.traderpro.domain.OrderType.STOP_LIMIT -> "Stop-limit orders trigger a limit order when the stop price is reached."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // P/L Display
            uiState.profitLoss?.let { pl ->
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    Text(
                        text = "P/L: ${currencyFormatter.format(pl)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (pl >= 0) LightGreen else LightRed
                    )
                }
            }
            
            // Loading Indicator
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
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
        
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Buy Confirmation Dialog
        if (uiState.showBuyConfirmation) {
            AlertDialog(
                onDismissRequest = onDismissBuyConfirmation,
                title = { Text("Confirm Buy") },
                text = { 
                    Text("Buy ${uiState.symbol} at 2x leverage with current price ${currencyFormatter.format(uiState.currentPrice)}?") 
                },
                confirmButton = {
                    Button(onClick = onConfirmBuy) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissBuyConfirmation) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Sell Confirmation Dialog
        if (uiState.showSellConfirmation) {
            AlertDialog(
                onDismissRequest = onDismissSellConfirmation,
                title = { Text("Confirm Sell") },
                text = { 
                    Text("Sell all shares of ${uiState.symbol}?") 
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmSell,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissSellConfirmation) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Celebration Animation
        if (uiState.showCelebration) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val animationRes = when (uiState.celebrationType) {
                    CelebrationType.BIG_WIN -> R.raw.celebration_big_win
                    CelebrationType.PROFIT -> R.raw.celebration_confetti
                    else -> R.raw.celebration_confetti
                }
                
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(animationRes)
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = 1
                )
                
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
                
                if (progress >= 0.99f) {
                    LaunchedEffect(Unit) {
                        onDismissCelebration()
                    }
                }
            }
        }
    }
}
