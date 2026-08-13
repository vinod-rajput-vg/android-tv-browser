package com.tvbrowser.browser

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.ProgressBar
import android.view.View
import com.tvbrowser.settings.PreferencesManager

class BrowserEngine(private val context: Context, private val preferencesManager: PreferencesManager) {
    private val adBlocker = AdBlocker(context)

    fun configureWebView(webView: WebView) {
        val settings = webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = getUserAgent()
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(true)
            mediaPlaybackRequiresUserGesture = preferencesManager.isMediaAutoPlay()
        }

        if (preferencesManager.isAdBlockEnabled()) {
            webView.addJavascriptInterface(adBlocker, "AdBlocker")
        }
    }

    fun createWebViewClient(progressBar: ProgressBar): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                
                if (preferencesManager.isAdBlockEnabled()) {
                    injectAdBlockingScripts(view)
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && !url.startsWith("http")) {
                    return true
                }
                return false
            }
        }
    }

    fun createWebChromeClient(progressBar: ProgressBar): WebChromeClient {
        return object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
    }

    private fun injectAdBlockingScripts(webView: WebView?) {
        val adBlockScript = adBlocker.getBlockingScript()
        webView?.evaluateJavascript(adBlockScript, null)
    }

    private fun getUserAgent(): String {
        return "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 TV"
    }
}
