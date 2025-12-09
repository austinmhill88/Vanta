package com.alpaca.traderpro.data.service

import android.util.Log
import com.alpaca.traderpro.data.model.TradeUpdate
import com.alpaca.traderpro.data.model.WebSocketAuth
import com.alpaca.traderpro.data.model.WebSocketSubscribe
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketService(
    private val apiKey: String,
    private val apiSecret: String
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    
    fun connectToLivePrices(symbol: String): Flow<TradeUpdate> = callbackFlow {
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened")
                
                // Authenticate
                val authMessage = WebSocketAuth(
                    key = apiKey,
                    secret = apiSecret
                )
                webSocket.send(gson.toJson(authMessage))
                
                // Subscribe to trades
                val subscribeMessage = WebSocketSubscribe(
                    trades = listOf(symbol),
                    quotes = listOf(symbol)
                )
                webSocket.send(gson.toJson(subscribeMessage))
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val jsonArray = gson.fromJson(text, List::class.java)
                    jsonArray.forEach { item ->
                        val jsonObject = item as? Map<*, *>
                        val type = jsonObject?.get("T") as? String
                        
                        if (type == "t") { // Trade update
                            val tradeUpdate = gson.fromJson(
                                gson.toJson(item),
                                TradeUpdate::class.java
                            )
                            trySend(tradeUpdate)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message: ${e.message}")
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection failed: ${t.message}")
                close(t)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection closed: $reason")
                close()
            }
        }
        
        val request = Request.Builder()
            .url("wss://stream.data.alpaca.markets/v2/sip")
            .build()
        
        webSocket = client.newWebSocket(request, listener)
        
        awaitClose {
            webSocket?.close(1000, "Client closed")
            webSocket = null
        }
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
    }
}
