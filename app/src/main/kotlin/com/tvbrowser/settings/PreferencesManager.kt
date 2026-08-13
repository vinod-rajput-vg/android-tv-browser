package com.tvbrowser.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isAdBlockEnabled(): Boolean = prefs.getBoolean("ad_blocker_enabled", true)
    fun setAdBlockEnabled(enabled: Boolean) = prefs.edit().putBoolean("ad_blocker_enabled", enabled).apply()
    fun isYtAdBlockEnabled(): Boolean = prefs.getBoolean("yt_ad_blocker_enabled", true)
    fun setYtAdBlockEnabled(enabled: Boolean) = prefs.edit().putBoolean("yt_ad_blocker_enabled", enabled).apply()
    fun isBlockTrackingEnabled(): Boolean = prefs.getBoolean("block_tracking_enabled", true)
    fun setBlockTrackingEnabled(enabled: Boolean) = prefs.edit().putBoolean("block_tracking_enabled", enabled).apply()
    fun isBlockScriptsEnabled(): Boolean = prefs.getBoolean("block_scripts_enabled", false)
    fun setBlockScriptsEnabled(enabled: Boolean) = prefs.edit().putBoolean("block_scripts_enabled", enabled).apply()

    fun getHomePage(): String = prefs.getString("home_page", "https://www.google.com") ?: "https://www.google.com"
    fun setHomePage(url: String) = prefs.edit().putString("home_page", url).apply()
    fun getTextSize(): Int = prefs.getInt("text_size", 100)
    fun setTextSize(size: Int) = prefs.edit().putInt("text_size", size).apply()
    fun getScreenSize(): Int = prefs.getInt("screen_size", 100)
    fun setScreenSize(size: Int) = prefs.edit().putInt("screen_size", size.coerceIn(25, 100)).apply()
    fun isJavaScriptEnabled(): Boolean = prefs.getBoolean("javascript_enabled", true)
    fun setJavaScriptEnabled(enabled: Boolean) = prefs.edit().putBoolean("javascript_enabled", enabled).apply()
    fun isPcModeEnabled(): Boolean = prefs.getBoolean("pc_mode_enabled", false)
    fun setPcModeEnabled(enabled: Boolean) = prefs.edit().putBoolean("pc_mode_enabled", enabled).apply()

    fun isCookiesEnabled(): Boolean = prefs.getBoolean("cookies_enabled", true)
    fun setCookiesEnabled(enabled: Boolean) = prefs.edit().putBoolean("cookies_enabled", enabled).apply()
    fun isCacheEnabled(): Boolean = prefs.getBoolean("cache_enabled", true)
    fun setCacheEnabled(enabled: Boolean) = prefs.edit().putBoolean("cache_enabled", enabled).apply()
    fun isHistoryEnabled(): Boolean = prefs.getBoolean("history_enabled", true)
    fun setHistoryEnabled(enabled: Boolean) = prefs.edit().putBoolean("history_enabled", enabled).apply()
    fun isMediaAutoPlay(): Boolean = prefs.getBoolean("media_autoplay_enabled", false)
    fun setMediaAutoPlay(enabled: Boolean) = prefs.edit().putBoolean("media_autoplay_enabled", enabled).apply()

    fun getCustomBlocklist(): Set<String> = prefs.getStringSet("custom_blocklist", setOf()) ?: setOf()
    fun setCustomBlocklist(domains: Set<String>) = prefs.edit().putStringSet("custom_blocklist", domains).apply()
    fun addToBlocklist(domain: String) { val list = getCustomBlocklist().toMutableSet(); list.add(domain); setCustomBlocklist(list) }
    fun removeFromBlocklist(domain: String) { val list = getCustomBlocklist().toMutableSet(); list.remove(domain); setCustomBlocklist(list) }

    fun getBookmarks(): List<Pair<String, String>> {
        return prefs.getStringSet("bookmarks", emptySet())!!.mapNotNull { value ->
            val separator = value.indexOf("||")
            if (separator <= 0) null else value.substring(0, separator) to value.substring(separator + 2)
        }.sortedBy { it.first.lowercase() }
    }

    fun addBookmark(title: String, url: String) {
        val list = prefs.getStringSet("bookmarks", emptySet())!!.toMutableSet()
        list.removeAll { it.endsWith("||$url") }
        list.add("${title.replace("||", " ")}||$url")
        prefs.edit().putStringSet("bookmarks", list).apply()
    }
}
