package com.tvbrowser.browser

import android.content.Context
import android.webkit.JavascriptInterface
import com.tvbrowser.utils.BlocklistManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class AdBlocker(private val context: Context) {
    private val blocklistManager = BlocklistManager(context)
    private val blockedDomains = mutableSetOf<String>()
    private val adPatterns = mutableListOf<Pattern>()

    init { loadBlocklists() }

    private fun loadBlocklists() {
        CoroutineScope(Dispatchers.IO).launch {
            addYouTubePatterns()
            addGeneralAdPatterns()
            blockedDomains.addAll(blocklistManager.getBlockedDomains())
        }
    }

    private fun addYouTubePatterns() {
        adPatterns.addAll(listOf(
            Pattern.compile(".*(^|[./])googleads([./]|$).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*doubleclick.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*adservice.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*youtube.*ads.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/get_video_info.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*pagead.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*googlesyndication.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*adclick.*", Pattern.CASE_INSENSITIVE)
        ))
    }

    private fun addGeneralAdPatterns() {
        adPatterns.addAll(listOf(
            Pattern.compile(".*/ads(?:/|\\?).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/ad(?:/|\\?).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*advertisement.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*googlesyndication.*", Pattern.CASE_INSENSITIVE)
        ))
    }

    @JavascriptInterface
    fun shouldBlockUrl(url: String): Boolean {
        val domain = extractDomain(url)
        if (blockedDomains.any { domain.contains(it) }) return true
        return adPatterns.any { it.matcher(url).matches() }
    }

    private fun extractDomain(url: String): String {
        return try {
            val start = url.indexOf("://") + 3
            val end = url.indexOf("/", start).let { if (it == -1) url.length else it }
            url.substring(start, end)
        } catch (_: Exception) { url }
    }

    fun getBlockingScript(): String = """
        (function() {
            const selectors = [
                'iframe[src*="doubleclick"]',
                'iframe[src*="googlesyndication"]',
                'iframe[src*="googleads"]',
                '[data-ad-client]',
                '[data-ad-slot]',
                '.ad-container',
                '.ad-placeholder'
            ];
            selectors.forEach(selector => {
                document.querySelectorAll(selector).forEach(el => {
                    el.style.display = 'none';
                    el.remove();
                });
            });
        })();
    """
}
