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
        webView.settings.apply {
            javaScriptEnabled = preferencesManager.isJavaScriptEnabled()
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
            cacheMode = if (preferencesManager.isCacheEnabled()) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_NO_CACHE
            setSupportZoom(true)
            textZoom = preferencesManager.getTextSize()
            mediaPlaybackRequiresUserGesture = !preferencesManager.isMediaAutoPlay()
        }

        webView.setInitialScale(preferencesManager.getScreenSize())

        if (preferencesManager.isAdBlockEnabled()) {
            webView.addJavascriptInterface(adBlocker, "AdBlocker")
        }
    }

    fun createWebViewClient(progressBar: ProgressBar, onUrlChanged: (String?) -> Unit): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
                onUrlChanged(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                onUrlChanged(url ?: view?.url)
                if (preferencesManager.isAdBlockEnabled()) injectAdBlockingScripts(view)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url != null && !url.startsWith("http://") && !url.startsWith("https://")
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
        webView?.evaluateJavascript(adBlocker.getBlockingScript(), null)
    }

    private fun getUserAgent(): String {
        return if (preferencesManager.isPcModeEnabled()) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        } else {
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 TV"
        }
    }
}
