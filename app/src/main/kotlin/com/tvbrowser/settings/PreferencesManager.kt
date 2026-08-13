package com.tvbrowser.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // Ad Blocking Settings
    fun isAdBlockEnabled(): Boolean = prefs.getBoolean("ad_blocker_enabled", true)
    fun setAdBlockEnabled(enabled: Boolean) = prefs.edit().putBoolean("ad_blocker_enabled", enabled).apply()

    fun isYtAdBlockEnabled(): Boolean = prefs.getBoolean("yt_ad_blocker_enabled", true)
    fun setYtAdBlockEnabled(enabled: Boolean) = prefs.edit().putBoolean("yt_ad_blocker_enabled", enabled).apply()

    fun isBlockTrackingEnabled(): Boolean = prefs.getBoolean("block_tracking_enabled", true)
    fun setBlockTrackingEnabled(enabled: Boolean) = prefs.edit().putBoolean("block_tracking_enabled", enabled).apply()

    fun isBlockScriptsEnabled(): Boolean = prefs.getBoolean("block_scripts_enabled", false)
    fun setBlockScriptsEnabled(enabled: Boolean) = prefs.edit().putBoolean("block_scripts_enabled", enabled).apply()

    // Browser Settings
    fun getHomePage(): String = prefs.getString("home_page", "https://www.google.com") ?: "https://www.google.com"
    fun setHomePage(url: String) = prefs.edit().putString("home_page", url).apply()

    fun getTextSize(): Int = prefs.getInt("text_size", 100)
    fun setTextSize(size: Int) = prefs.edit().putInt("text_size", size).apply()

    fun isJavaScriptEnabled(): Boolean = prefs.getBoolean("javascript_enabled", true)
    fun setJavaScriptEnabled(enabled: Boolean) = prefs.edit().putBoolean("javascript_enabled", enabled).apply()

    fun isPcModeEnabled(): Boolean = prefs.getBoolean("pc_mode_enabled", false)
    fun setPcModeEnabled(enabled: Boolean) = prefs.edit().putBoolean("pc_mode_enabled", enabled).apply()

    // Privacy Settings
    fun isCookiesEnabled(): Boolean = prefs.getBoolean("cookies_enabled", true)
    fun setCookiesEnabled(enabled: Boolean) = prefs.edit().putBoolean("cookies_enabled", enabled).apply()

    fun isCacheEnabled(): Boolean = prefs.getBoolean("cache_enabled", true)
    fun setCacheEnabled(enabled: Boolean) = prefs.edit().putBoolean("cache_enabled", enabled).apply()

    fun isHistoryEnabled(): Boolean = prefs.getBoolean("history_enabled", true)
    fun setHistoryEnabled(enabled: Boolean) = prefs.edit().putBoolean("history_enabled", enabled).apply()

    // Media Settings
    fun isMediaAutoPlay(): Boolean = prefs.getBoolean("media_autoplay_enabled", false)
    fun setMediaAutoPlay(enabled: Boolean) = prefs.edit().putBoolean("media_autoplay_enabled", enabled).apply()

    // Custom Blocklist
    fun getCustomBlocklist(): Set<String> = prefs.getStringSet("custom_blocklist", setOf()) ?: setOf()
    fun setCustomBlocklist(domains: Set<String>) = prefs.edit().putStringSet("custom_blocklist", domains).apply()

    fun addToBlocklist(domain: String) {
        val blocklist = getCustomBlocklist().toMutableSet()
        blocklist.add(domain)
        setCustomBlocklist(blocklist)
    }

    fun removeFromBlocklist(domain: String) {
        val blocklist = getCustomBlocklist().toMutableSet()
        blocklist.remove(domain)
        setCustomBlocklist(blocklist)
    }
}
