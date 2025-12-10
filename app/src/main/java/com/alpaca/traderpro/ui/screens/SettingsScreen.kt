package com.alpaca.traderpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.alpaca.traderpro.R
import com.alpaca.traderpro.domain.SettingsUiState
import com.alpaca.traderpro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onApiKeyChange: (String) -> Unit,
    onApiSecretChange: (String) -> Unit,
    onBuyWindowStartChange: (String) -> Unit,
    onBuyWindowEndChange: (String) -> Unit,
    onSellWindowStartChange: (String) -> Unit,
    onSellWindowEndChange: (String) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onSuggestWindows: () -> Unit,
    onSaveSettings: () -> Unit,
    onAutoModeEnabledChange: (Boolean) -> Unit,
    onSellByTimeChange: (String) -> Unit,
    onTargetPercentChange: (Float) -> Unit,
    onStopPercentChange: (Float) -> Unit,
    onUseVWAPFilterChange: (Boolean) -> Unit,
    onConfirmAutoMode: () -> Unit,
    onCancelAutoMode: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // AUTO MODE TOGGLE - Prominent at the very top
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.autoModeEnabled) PrimaryGreen else PrimaryRed
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Rocket,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (uiState.autoModeEnabled) LightGreen else LightRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Auto-Trade Engine",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (uiState.autoModeEnabled) LightGreen else LightRed
                            )
                            Text(
                                text = if (uiState.autoModeEnabled) "ON" else "OFF",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        }
                    }
                    
                    Switch(
                        checked = uiState.autoModeEnabled,
                        onCheckedChange = onAutoModeEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LightGreen,
                            checkedTrackColor = AccentGreen,
                            uncheckedThumbColor = LightRed,
                            uncheckedTrackColor = AccentRed
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Auto-Mode Settings (visible when enabled)
            AnimatedVisibility(
                visible = uiState.autoModeEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Auto-Trade Settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = AccentBlue
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.sellByTime,
                            onValueChange = onSellByTimeChange,
                            label = { Text("Force Exit Time (HH:mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = uiState.targetPercent.toString(),
                            onValueChange = { 
                                it.toFloatOrNull()?.let { value -> onTargetPercentChange(value) }
                            },
                            label = { Text("Target % (Profit)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            suffix = { Text("%") }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = uiState.stopPercent.toString(),
                            onValueChange = { 
                                it.toFloatOrNull()?.let { value -> onStopPercentChange(value) }
                            },
                            label = { Text("Stop % (Loss)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            suffix = { Text("%") }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Use VWAP Filter",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = uiState.useVWAPFilter,
                                onCheckedChange = onUseVWAPFilterChange
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // API Credentials Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = AccentBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "API Credentials",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.apiKey,
                        onValueChange = onApiKeyChange,
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = uiState.apiSecret,
                        onValueChange = onApiSecretChange,
                        label = { Text("API Secret") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Time Windows Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AccentGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Time Windows",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Buy Window",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightGreen
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.buyWindowStart,
                            onValueChange = onBuyWindowStartChange,
                            label = { Text("Start (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = uiState.buyWindowEnd,
                            onValueChange = onBuyWindowEndChange,
                            label = { Text("End (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Sell Window",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightRed
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.sellWindowStart,
                            onValueChange = onSellWindowStartChange,
                            label = { Text("Start (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = uiState.sellWindowEnd,
                            onValueChange = onSellWindowEndChange,
                            label = { Text("End (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onSuggestWindows,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue
                        )
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Suggest Windows from History")
                    }
                    
                    // Display Suggestions
                    AnimatedVisibility(
                        visible = uiState.suggestedBuyWindow.isNotEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = SurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Suggested Buy Window:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OnSurfaceVariant
                                    )
                                    Text(
                                        text = uiState.suggestedBuyWindow,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = LightGreen
                                    )
                                    
                                    if (uiState.suggestedSellWindow.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Suggested Sell Window:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OnSurfaceVariant
                                        )
                                        Text(
                                            text = uiState.suggestedSellWindow,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = LightRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Notifications Section
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
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = AccentBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onNotificationsEnabledChange
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save Button
            Button(
                onClick = onSaveSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error Message
            uiState.errorMessage?.let { error ->
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
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Success Celebration
        if (uiState.saveSuccess) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.success_checkmark)
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = 1
                )
                
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(200.dp)
                )
            }
        }
        
        // Auto-Mode Confirmation Dialog
        if (uiState.showAutoModeConfirmation) {
            AlertDialog(
                onDismissRequest = onCancelAutoMode,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { 
                    Text(
                        "Enable Auto-Trade?",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                text = { 
                    Text(
                        "This will trade real money automatically. Are you sure?",
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmAutoMode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen
                        )
                    ) {
                        Icon(Icons.Default.Rocket, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yes, Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelAutoMode) {
                        Text("Cancel")
                    }
                },
                containerColor = SurfaceDark
            )
        }
    }
}
