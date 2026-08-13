package com.tvbrowser.browser

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.tvbrowser.utils.BlocklistManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class AdBlocker(private val context: Context) {
    private val blocklistManager = BlocklistManager(context)
    private val blockedDomains = mutableSetOf<String>()
    private val adPatterns = mutableListOf<Pattern>()

    init {
        loadBlocklists()
    }

    private fun loadBlocklists() {
        CoroutineScope(Dispatchers.IO).launch {
            // YouTube ad patterns
            addYouTubePatterns()
            // General ad patterns
            addGeneralAdPatterns()
            // Load from user blocklists
            blockedDomains.addAll(blocklistManager.getBlockedDomains())
        }
    }

    private fun addYouTubePatterns() {
        val ytPatterns = listOf(
            Pattern.compile(".*(google)?ads.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*doubleclick.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*adservice.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*youtube.*ads.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/get_video_info.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*pagead.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*googlesyndication.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*adclick.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*tracking.*", Pattern.CASE_INSENSITIVE)
        )
        adPatterns.addAll(ytPatterns)
    }

    private fun addGeneralAdPatterns() {
        val generalPatterns = listOf(
            Pattern.compile(".*/ads/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*banner.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/ad/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*advertisement.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*advert.*", Pattern.CASE_INSENSITIVE)
        )
        adPatterns.addAll(generalPatterns)
    }

    fun shouldBlockUrl(url: String): Boolean {
        val domain = extractDomain(url)
        
        // Check against blocklist
        if (blockedDomains.any { domain.contains(it) }) {
            return true
        }
        
        // Check against patterns
        return adPatterns.any { it.matcher(url).matches() }
    }

    private fun extractDomain(url: String): String {
        return try {
            val start = url.indexOf("://") + 3
            val end = url.indexOf("/", start).let { if (it == -1) url.length else it }
            url.substring(start, end)
        } catch (e: Exception) {
            url
        }
    }

    fun getBlockingScript(): String {
        return """
            (function() {
                // Hide ad elements
                const selectors = [
                    '[class*="ad"]',
                    '[id*="ad"]',
                    '[class*="banner"]',
                    '[class*="advert"]',
                    'iframe[src*="ads"]',
                    'iframe[src*="doubleclick"]',
                    'iframe[src*="googlesyndication"]',
                    '.ad-container',
                    '.ad-placeholder',
                    '[data-ad-client]',
                    '[data-ad-slot]'
                ];
                
                selectors.forEach(selector => {
                    document.querySelectorAll(selector).forEach(el => {
                        el.style.display = 'none';
                        el.remove();
                    });
                });

                // Block ad requests
                if (window.XMLHttpRequest) {
                    const originalOpen = XMLHttpRequest.prototype.open;
                    XMLHttpRequest.prototype.open = function(method, url, ...args) {
                        if (this.shouldBlockUrl && this.shouldBlockUrl(url)) {
                            console.log('Blocked ad request: ' + url);
                            return;
                        }
                        return originalOpen.apply(this, [method, url, ...args]);
                    };
                }
            })();
        """
    }

    @JavascriptInterface
    fun shouldBlockUrl(url: String): Boolean {
        return shouldBlockUrl(url)
    }
}
