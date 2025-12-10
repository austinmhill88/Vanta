package com.alpaca.traderpro.data.service

import com.alpaca.traderpro.data.model.Account
import com.alpaca.traderpro.data.model.Order
import com.alpaca.traderpro.data.model.OrderRequest
import com.alpaca.traderpro.data.model.Position
import retrofit2.Response
import retrofit2.http.*

interface AlpacaApiService {
    @GET("v2/account")
    suspend fun getAccount(): Response<Account>
    
    @GET("v2/positions/{symbol}")
    suspend fun getPosition(@Path("symbol") symbol: String): Response<Position>
    
    @GET("v2/positions")
    suspend fun getAllPositions(): Response<List<Position>>
    
    @POST("v2/orders")
    suspend fun createOrder(@Body orderRequest: OrderRequest): Response<Order>
    
    @GET("v2/orders")
    suspend fun getOrders(): Response<List<Order>>
    
    @DELETE("v2/orders/{id}")
    suspend fun cancelOrder(@Path("id") orderId: String): Response<Unit>
}
