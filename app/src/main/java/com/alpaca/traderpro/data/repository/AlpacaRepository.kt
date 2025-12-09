package com.alpaca.traderpro.data.repository

import com.alpaca.traderpro.data.model.Account
import com.alpaca.traderpro.data.model.Order
import com.alpaca.traderpro.data.model.OrderRequest
import com.alpaca.traderpro.data.model.Position
import com.alpaca.traderpro.data.service.AlpacaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlpacaRepository(
    private val apiService: AlpacaApiService
) {
    
    suspend fun getAccount(): Result<Account> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAccount()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get account: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPosition(symbol: String): Result<Position> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPosition(symbol)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("No position found for $symbol"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createOrder(orderRequest: OrderRequest): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createOrder(orderRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Order failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun buyWithLeverage(
        symbol: String,
        buyingPower: Double,
        currentPrice: Double
    ): Result<Order> {
        val leveragedAmount = buyingPower * 2
        val quantity = (leveragedAmount / currentPrice).toInt()
        
        if (quantity <= 0) {
            return Result.failure(Exception("Insufficient funds to buy"))
        }
        
        val orderRequest = OrderRequest(
            symbol = symbol,
            qty = quantity.toString(),
            side = "buy",
            type = "market",
            timeInForce = "day"
        )
        
        return createOrder(orderRequest)
    }
    
    suspend fun sellAll(symbol: String): Result<Order> = withContext(Dispatchers.IO) {
        val positionResult = getPosition(symbol)
        
        positionResult.fold(
            onSuccess = { position ->
                val orderRequest = OrderRequest(
                    symbol = symbol,
                    qty = position.qty,
                    side = "sell",
                    type = "market",
                    timeInForce = "day"
                )
                createOrder(orderRequest)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }
}
