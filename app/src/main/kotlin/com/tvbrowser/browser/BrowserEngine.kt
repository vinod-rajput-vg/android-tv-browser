package com.tvbrowser.browser

import android.content.Context
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.tvbrowser.settings.PreferencesManager

class BrowserEngine(private val context: Context, private val preferencesManager: PreferencesManager) {
    private val adBlocker = AdBlocker(context)

    fun configureWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = preferencesManager.isJavaScriptEnabled()
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = getUserAgent()
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            textZoom = preferencesManager.getTextSize()
            mediaPlaybackRequiresUserGesture = !preferencesManager.isMediaAutoPlay()
            loadsImagesAutomatically = true
            blockNetworkLoads = false
            javaScriptCanOpenWindowsAutomatically = true
        }
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.setInitialScale(preferencesManager.getScreenSize())
        if (preferencesManager.isAdBlockEnabled()) webView.addJavascriptInterface(adBlocker, "AdBlocker")
    }

    fun createWebViewClient(progressBar: ProgressBar, onUrlChanged: (String?) -> Unit): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
                onUrlChanged(url)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                onUrlChanged(url ?: view?.url)
                if (preferencesManager.isAdBlockEnabled()) view?.evaluateJavascript(adBlocker.getBlockingScript(), null)
            }
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?) = false
        }
    }

    fun createWebChromeClient(progressBar: ProgressBar, onShowCustomView: (View, WebChromeClient.CustomViewCallback) -> Unit, onHideCustomView: () -> Unit): WebChromeClient {
        return object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) { progressBar.progress = newProgress }
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view != null && callback != null) onShowCustomView(view, callback) else callback?.onCustomViewHidden()
            }
            override fun onHideCustomView() { onHideCustomView() }
        }
    }

    private fun getUserAgent(): String = if (preferencesManager.isPcModeEnabled()) {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    } else {
        "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 TV"
    }
}
