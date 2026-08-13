package com.tvbrowser.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader

class BlocklistManager(private val context: Context) {
    private val client = OkHttpClient()
    private val cachedBlocklist = mutableSetOf<String>()
    private var lastUpdateTime = 0L

    private val adBlockLists = listOf(
        "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        "https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt",
        "https://someonewhocares.org/hosts/hosts"
    )

    suspend fun getBlockedDomains(): Set<String> = withContext(Dispatchers.IO) {
        if (shouldRefreshBlocklist()) {
            refreshBlocklist()
        }
        cachedBlocklist
    }

    private fun shouldRefreshBlocklist(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUpdateTime) > 24 * 60 * 60 * 1000 // 24 hours
    }

    private suspend fun refreshBlocklist() = withContext(Dispatchers.IO) {
        cachedBlocklist.clear()
        for (url in adBlockLists) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.byteStream().use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.lineSequence()
                                .filter { it.isNotBlank() && !it.startsWith("#") }
                                .map { line ->
                                    val parts = line.trim().split("\\s+".toRegex())
                                    if (parts.size > 1) parts[1] else parts[0]
                                }
                                .filter { it.contains(".") && !it.startsWith(".") }
                                .forEach { cachedBlocklist.add(it) }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        lastUpdateTime = System.currentTimeMillis()
    }

    fun addCustomDomain(domain: String) {
        cachedBlocklist.add(domain.trim().lowercase())
    }

    fun removeDomain(domain: String) {
        cachedBlocklist.remove(domain.trim().lowercase())
    }
}
