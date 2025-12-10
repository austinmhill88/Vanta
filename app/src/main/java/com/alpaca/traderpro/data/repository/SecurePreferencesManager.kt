package com.alpaca.traderpro.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferencesManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }
    
    fun saveApiSecret(apiSecret: String) {
        sharedPreferences.edit().putString(KEY_API_SECRET, apiSecret).apply()
    }
    
    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, null)
    }
    
    fun getApiSecret(): String? {
        return sharedPreferences.getString(KEY_API_SECRET, null)
    }
    
    fun hasCredentials(): Boolean {
        return !getApiKey().isNullOrEmpty() && !getApiSecret().isNullOrEmpty()
    }
    
    fun clearCredentials() {
        sharedPreferences.edit()
            .remove(KEY_API_KEY)
            .remove(KEY_API_SECRET)
            .apply()
    }
    
    fun saveBuyWindowStart(time: String) {
        sharedPreferences.edit().putString(KEY_BUY_WINDOW_START, time).apply()
    }
    
    fun saveBuyWindowEnd(time: String) {
        sharedPreferences.edit().putString(KEY_BUY_WINDOW_END, time).apply()
    }
    
    fun saveSellWindowStart(time: String) {
        sharedPreferences.edit().putString(KEY_SELL_WINDOW_START, time).apply()
    }
    
    fun saveSellWindowEnd(time: String) {
        sharedPreferences.edit().putString(KEY_SELL_WINDOW_END, time).apply()
    }
    
    fun getBuyWindowStart(): String? {
        return sharedPreferences.getString(KEY_BUY_WINDOW_START, null)
    }
    
    fun getBuyWindowEnd(): String? {
        return sharedPreferences.getString(KEY_BUY_WINDOW_END, null)
    }
    
    fun getSellWindowStart(): String? {
        return sharedPreferences.getString(KEY_SELL_WINDOW_START, null)
    }
    
    fun getSellWindowEnd(): String? {
        return sharedPreferences.getString(KEY_SELL_WINDOW_END, null)
    }
    
    fun saveNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    // Auto-Mode Settings
    fun saveAutoModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_MODE_ENABLED, enabled).apply()
    }
    
    fun isAutoModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_MODE_ENABLED, false)
    }
    
    fun saveSellByTime(time: String) {
        sharedPreferences.edit().putString(KEY_SELL_BY_TIME, time).apply()
    }
    
    fun getSellByTime(): String {
        return sharedPreferences.getString(KEY_SELL_BY_TIME, "14:30") ?: "14:30"
    }
    
    fun saveTargetPercent(percent: Float) {
        sharedPreferences.edit().putFloat(KEY_TARGET_PERCENT, percent).apply()
    }
    
    fun getTargetPercent(): Float {
        return sharedPreferences.getFloat(KEY_TARGET_PERCENT, 0.32f)
    }
    
    fun saveStopPercent(percent: Float) {
        sharedPreferences.edit().putFloat(KEY_STOP_PERCENT, percent).apply()
    }
    
    fun getStopPercent(): Float {
        return sharedPreferences.getFloat(KEY_STOP_PERCENT, 0.25f)
    }
    
    fun saveUseVWAPFilter(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_USE_VWAP_FILTER, enabled).apply()
    }
    
    fun useVWAPFilter(): Boolean {
        return sharedPreferences.getBoolean(KEY_USE_VWAP_FILTER, true)
    }
    
    fun saveAutoModeConfirmed(confirmed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_MODE_CONFIRMED, confirmed).apply()
    }
    
    fun isAutoModeConfirmed(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_MODE_CONFIRMED, false)
    }
    
    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_API_SECRET = "api_secret"
        private const val KEY_BUY_WINDOW_START = "buy_window_start"
        private const val KEY_BUY_WINDOW_END = "buy_window_end"
        private const val KEY_SELL_WINDOW_START = "sell_window_start"
        private const val KEY_SELL_WINDOW_END = "sell_window_end"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_AUTO_MODE_ENABLED = "auto_mode_enabled"
        private const val KEY_SELL_BY_TIME = "sell_by_time"
        private const val KEY_TARGET_PERCENT = "target_percent"
        private const val KEY_STOP_PERCENT = "stop_percent"
        private const val KEY_USE_VWAP_FILTER = "use_vwap_filter"
        private const val KEY_AUTO_MODE_CONFIRMED = "auto_mode_confirmed"
    }
}
