package com.alpaca.traderpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alpaca.traderpro.data.database.AlpacaDatabase
import com.alpaca.traderpro.data.repository.AlpacaRepository
import com.alpaca.traderpro.data.repository.LogsRepository
import com.alpaca.traderpro.data.repository.SecurePreferencesManager
import com.alpaca.traderpro.data.service.AlpacaApiService
import com.alpaca.traderpro.domain.HomeViewModel
import com.alpaca.traderpro.domain.LogsViewModel
import com.alpaca.traderpro.domain.SettingsViewModel
import com.alpaca.traderpro.ui.navigation.Screen
import com.alpaca.traderpro.ui.screens.HomeScreen
import com.alpaca.traderpro.ui.screens.LogsScreen
import com.alpaca.traderpro.ui.screens.OnboardingScreen
import com.alpaca.traderpro.ui.screens.SettingsScreen
import com.alpaca.traderpro.ui.theme.AlpacaTraderProTheme
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    
    private lateinit var securePreferencesManager: SecurePreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        securePreferencesManager = SecurePreferencesManager(this)
        
        setContent {
            AlpacaTraderProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        securePreferencesManager = securePreferencesManager
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    securePreferencesManager: SecurePreferencesManager
) {
    val navController = rememberNavController()
    val startDestination = if (securePreferencesManager.hasCredentials()) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }
    
    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            
            if (currentRoute != Screen.Onboarding.route) {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
                        BottomNavItem("Portfolio", Screen.Portfolio.route, Icons.Default.AccountBalance),
                        BottomNavItem("Logs", Screen.Logs.route, Icons.Default.Assessment),
                        BottomNavItem("Settings", Screen.Settings.route, Icons.Default.Settings)
                    )
                    
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { apiKey, apiSecret ->
                        securePreferencesManager.saveApiKey(apiKey)
                        securePreferencesManager.saveApiSecret(apiSecret)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Home.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val app = context.applicationContext as AlpacaTraderApp
                
                val apiKey = securePreferencesManager.getApiKey() ?: ""
                val apiSecret = securePreferencesManager.getApiSecret() ?: ""
                
                val apiService = createApiService(apiKey, apiSecret)
                val alpacaRepository = AlpacaRepository(apiService)
                val logsRepository = LogsRepository(app.database.dailyLogDao())
                
                val viewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(
                        alpacaRepository,
                        logsRepository,
                        securePreferencesManager
                    )
                )
                
                val uiState by viewModel.uiState.collectAsState()
                
                HomeScreen(
                    uiState = uiState,
                    onSymbolChange = viewModel::updateSymbol,
                    onRefresh = viewModel::refresh,
                    onBuyClick = viewModel::showBuyConfirmation,
                    onSellClick = viewModel::showSellConfirmation,
                    onConfirmBuy = viewModel::executeBuy,
                    onConfirmSell = viewModel::executeSell,
                    onDismissBuyConfirmation = viewModel::hideBuyConfirmation,
                    onDismissSellConfirmation = viewModel::hideSellConfirmation,
                    onDismissCelebration = viewModel::dismissCelebration,
                    onToggleAdvancedTrading = viewModel::toggleAdvancedTrading,
                    onCustomQuantityChange = viewModel::updateCustomQuantity,
                    onOrderTypeChange = viewModel::updateOrderType,
                    onLimitPriceChange = viewModel::updateLimitPrice,
                    onStopPriceChange = viewModel::updateStopPrice
                )
            }
            
            composable(Screen.Portfolio.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val app = context.applicationContext as AlpacaTraderApp
                
                val apiKey = securePreferencesManager.getApiKey() ?: ""
                val apiSecret = securePreferencesManager.getApiSecret() ?: ""
                
                val apiService = createApiService(apiKey, apiSecret)
                val alpacaRepository = AlpacaRepository(apiService)
                
                val viewModel: com.alpaca.traderpro.domain.PortfolioViewModel = viewModel(
                    factory = PortfolioViewModelFactory(alpacaRepository)
                )
                
                val uiState by viewModel.uiState.collectAsState()
                
                com.alpaca.traderpro.ui.screens.PortfolioScreen(
                    uiState = uiState,
                    onRefresh = viewModel::refresh,
                    onPositionClick = { symbol ->
                        // Navigate to home screen with this symbol
                        navController.navigate(Screen.Home.route)
                    },
                    onClosePosition = viewModel::closePosition
                )
            }
            
            composable(Screen.Logs.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val app = context.applicationContext as AlpacaTraderApp
                val logsRepository = LogsRepository(app.database.dailyLogDao())
                
                val viewModel: LogsViewModel = viewModel(
                    factory = LogsViewModelFactory(logsRepository)
                )
                
                val uiState by viewModel.uiState.collectAsState()
                
                LogsScreen(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onExportClick = { viewModel.exportToCsv(context.filesDir) },
                    onDeleteLog = viewModel::deleteLog
                )
            }
            
            composable(Screen.Settings.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val app = context.applicationContext as AlpacaTraderApp
                val logsRepository = LogsRepository(app.database.dailyLogDao())
                
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(
                        securePreferencesManager,
                        logsRepository
                    )
                )
                
                val uiState by viewModel.uiState.collectAsState()
                
                SettingsScreen(
                    uiState = uiState,
                    onApiKeyChange = viewModel::updateApiKey,
                    onApiSecretChange = viewModel::updateApiSecret,
                    onBuyWindowStartChange = viewModel::updateBuyWindowStart,
                    onBuyWindowEndChange = viewModel::updateBuyWindowEnd,
                    onSellWindowStartChange = viewModel::updateSellWindowStart,
                    onSellWindowEndChange = viewModel::updateSellWindowEnd,
                    onNotificationsEnabledChange = viewModel::updateNotificationsEnabled,
                    onSuggestWindows = viewModel::suggestTimeWindows,
                    onSaveSettings = viewModel::saveSettings
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun createApiService(apiKey: String, apiSecret: String): AlpacaApiService {
    val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("APCA-API-KEY-ID", apiKey)
            .addHeader("APCA-API-SECRET-KEY", apiSecret)
            .build()
        chain.proceed(request)
    }
    
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.alpaca.markets/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    return retrofit.create(AlpacaApiService::class.java)
}

// ViewModelFactories
class HomeViewModelFactory(
    private val alpacaRepository: AlpacaRepository,
    private val logsRepository: LogsRepository,
    private val securePreferencesManager: SecurePreferencesManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(alpacaRepository, logsRepository, securePreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PortfolioViewModelFactory(
    private val alpacaRepository: AlpacaRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.alpaca.traderpro.domain.PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.alpaca.traderpro.domain.PortfolioViewModel(alpacaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LogsViewModelFactory(
    private val logsRepository: LogsRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogsViewModel(logsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val securePreferencesManager: SecurePreferencesManager,
    private val logsRepository: LogsRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(securePreferencesManager, logsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
