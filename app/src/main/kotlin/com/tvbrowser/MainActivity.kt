package com.tvbrowser

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.tvbrowser.browser.BrowserEngine
import com.tvbrowser.settings.PreferencesManager

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var browserEngine: BrowserEngine
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var btnHome: ImageButton
    private var pcModeAtLastSetup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesManager = PreferencesManager(this)
        browserEngine = BrowserEngine(this, preferencesManager)

        initViews()
        setupListeners()
        setupWebView()
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnSettings = findViewById(R.id.btnSettings)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun setupWebView() {
        browserEngine.configureWebView(webView)
        webView.webViewClient = browserEngine.createWebViewClient(progressBar)
        webView.webChromeClient = browserEngine.createWebChromeClient(progressBar)
        pcModeAtLastSetup = preferencesManager.isPcModeEnabled()
        loadHomePage()
    }

    private fun setupListeners() {
        urlBar.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                navigateToUrl(urlBar.text.toString())
                return@setOnKeyListener true
            }
            false
        }

        btnBack.setOnClickListener { webView.goBack() }
        btnForward.setOnClickListener { webView.goForward() }
        btnSettings.setOnClickListener { openSettings() }
        btnHome.setOnClickListener { loadHomePage() }
    }

    private fun navigateToUrl(url: String) {
        var finalUrl = url.trim()
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }
        webView.loadUrl(finalUrl)
    }

    private fun loadHomePage() {
        val homePage = preferencesManager.getHomePage()
        webView.loadUrl(homePage)
        urlBar.setText(homePage)
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()

        val pcModeEnabled = preferencesManager.isPcModeEnabled()
        if (pcModeEnabled != pcModeAtLastSetup) {
            val currentUrl = webView.url ?: preferencesManager.getHomePage()
            browserEngine.configureWebView(webView)
            pcModeAtLastSetup = pcModeEnabled
            webView.loadUrl(currentUrl)
        }
    }

    override fun onPause() {
        webView.onPause()
        webView.pauseTimers()
        super.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
