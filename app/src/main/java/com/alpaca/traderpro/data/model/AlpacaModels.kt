package com.alpaca.traderpro.data.model

import com.google.gson.annotations.SerializedName

data class Account(
    @SerializedName("buying_power")
    val buyingPower: String,
    @SerializedName("cash")
    val cash: String,
    @SerializedName("portfolio_value")
    val portfolioValue: String,
    @SerializedName("equity")
    val equity: String
)

data class Position(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("qty")
    val qty: String,
    @SerializedName("avg_entry_price")
    val avgEntryPrice: String,
    @SerializedName("current_price")
    val currentPrice: String,
    @SerializedName("market_value")
    val marketValue: String,
    @SerializedName("unrealized_pl")
    val unrealizedPl: String,
    @SerializedName("unrealized_plpc")
    val unrealizedPlpc: String
)

data class Order(
    @SerializedName("id")
    val id: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("qty")
    val qty: String,
    @SerializedName("side")
    val side: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("time_in_force")
    val timeInForce: String,
    @SerializedName("status")
    val status: String
)

data class OrderRequest(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("qty")
    val qty: String,
    @SerializedName("side")
    val side: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("time_in_force")
    val timeInForce: String,
    @SerializedName("limit_price")
    val limitPrice: String? = null,
    @SerializedName("stop_price")
    val stopPrice: String? = null
)

data class WebSocketAuth(
    @SerializedName("action")
    val action: String = "auth",
    @SerializedName("key")
    val key: String,
    @SerializedName("secret")
    val secret: String
)

data class WebSocketSubscribe(
    @SerializedName("action")
    val action: String = "subscribe",
    @SerializedName("trades")
    val trades: List<String> = emptyList(),
    @SerializedName("quotes")
    val quotes: List<String> = emptyList()
)

data class TradeUpdate(
    @SerializedName("T")
    val type: String,
    @SerializedName("S")
    val symbol: String,
    @SerializedName("p")
    val price: Double,
    @SerializedName("t")
    val timestamp: String
)

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val timestamp: Long
)

data class AutoTradeSignal(
    val symbol: String,
    val signalTime: String,
    val entryPrice: Double,
    val targetPrice: Double,
    val stopPrice: Double,
    val signalType: String = "SHORT"
)

data class LiveTrade(
    val symbol: String,
    val entryPrice: Double,
    val targetPrice: Double,
    val stopPrice: Double,
    val quantity: Int,
    val entryTime: Long,
    val isAutoTrade: Boolean = false
)

data class BracketOrderRequest(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("qty")
    val qty: String,
    @SerializedName("side")
    val side: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("time_in_force")
    val timeInForce: String,
    @SerializedName("order_class")
    val orderClass: String = "bracket",
    @SerializedName("take_profit")
    val takeProfit: TakeProfitLeg,
    @SerializedName("stop_loss")
    val stopLoss: StopLossLeg
)

data class TakeProfitLeg(
    @SerializedName("limit_price")
    val limitPrice: String
)

data class StopLossLeg(
    @SerializedName("stop_price")
    val stopPrice: String
)
