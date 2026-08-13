package com.tvbrowser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tvbrowser.R
import com.tvbrowser.browser.BrowserEngine
import com.tvbrowser.settings.PreferencesManager
import android.widget.Toast
import android.content.ActivityNotFoundException

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var browserEngine: BrowserEngine
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnVoice: ImageButton
    private lateinit var btnMenu: ImageButton
    private var pcModeAtLastSetup = false
    private val voiceRequestCode = 7001
    private val audioPermissionCode = 7002

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
        btnHome = findViewById(R.id.btnHome)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnVoice = findViewById(R.id.btnVoice)
        btnMenu = findViewById(R.id.btnMenu)
    }

    private fun setupWebView() {
        browserEngine.configureWebView(webView)
        webView.webViewClient = browserEngine.createWebViewClient(progressBar) { url -> updateUrlBar(url) }
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

        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnHome.setOnClickListener { loadHomePage() }
        btnRefresh.setOnClickListener { webView.reload() }
        btnVoice.setOnClickListener { startVoiceSearch() }
        btnMenu.setOnClickListener { showBrowserMenu() }
    }

    private fun navigateToUrl(url: String) {
        val input = url.trim()
        if (input.isEmpty()) return
        val finalUrl = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=${android.net.Uri.encode(input)}"
        }
        webView.loadUrl(finalUrl)
    }

    private fun loadHomePage() {
        webView.loadUrl(preferencesManager.getHomePage())
    }

    private fun updateUrlBar(url: String?) {
        if (url.isNullOrBlank()) return
        if (urlBar.text.toString() != url) {
            urlBar.setText(url)
            urlBar.setSelection(urlBar.text.length)
        }
    }

    private fun showBrowserMenu() {
        val popup = PopupMenu(this, btnMenu)
        popup.menuInflater.inflate(R.menu.browser_menu, popup.menu)
        popup.menu.findItem(R.id.menu_pc_mode).isChecked = preferencesManager.isPcModeEnabled()

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_settings -> openSettings()
                R.id.menu_bookmarks -> showBookmarks()
                R.id.menu_add_bookmark -> addCurrentBookmark()
                R.id.menu_pc_mode -> {
                    val enabled = !preferencesManager.isPcModeEnabled()
                    preferencesManager.setPcModeEnabled(enabled)
                    item.isChecked = enabled
                    reloadWithCurrentSettings()
                }
                R.id.menu_text_zoom_in -> changeTextZoom(10)
                R.id.menu_text_zoom_out -> changeTextZoom(-10)
                R.id.menu_share -> shareCurrentPage()
                R.id.menu_external_browser -> openExternalBrowser()
                else -> return@setOnMenuItemClickListener false
            }
            true
        }
        popup.show()
    }

    private fun reloadWithCurrentSettings() {
        val currentUrl = webView.url ?: preferencesManager.getHomePage()
        browserEngine.configureWebView(webView)
        pcModeAtLastSetup = preferencesManager.isPcModeEnabled()
        webView.loadUrl(currentUrl)
    }

    private fun addCurrentBookmark() {
        val url = webView.url ?: return
        val title = webView.title?.takeIf { it.isNotBlank() } ?: url
        preferencesManager.addBookmark(title, url)
        Toast.makeText(this, getString(R.string.bookmark_added), Toast.LENGTH_SHORT).show()
    }

    private fun showBookmarks() {
        val bookmarks = preferencesManager.getBookmarks()
        if (bookmarks.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_bookmarks), Toast.LENGTH_SHORT).show()
            return
        }

        val labels = bookmarks.map { "${it.first}\n${it.second}" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.bookmarks)
            .setItems(labels) { _, which -> navigateToUrl(bookmarks[which].second) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun changeTextZoom(delta: Int) {
        val next = (webView.settings.textZoom + delta).coerceIn(25, 200)
        webView.settings.textZoom = next
        preferencesManager.setTextSize(next)
    }

    private fun shareCurrentPage() {
        val url = webView.url ?: return
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }, getString(R.string.share_page)))
    }

    private fun openExternalBrowser() {
        val url = webView.url ?: return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVoiceSearch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), audioPermissionCode)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_search_prompt))
        }
        try {
            startActivityForResult(intent, voiceRequestCode)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.voice_search_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Android activity result API retained for TV/legacy compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == voiceRequestCode && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!result.isNullOrBlank()) {
                urlBar.setText(result)
                navigateToUrl(result)
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == audioPermissionCode && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startVoiceSearch()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()

        val pcModeEnabled = preferencesManager.isPcModeEnabled()
        if (pcModeEnabled != pcModeAtLastSetup) reloadWithCurrentSettings()
        updateUrlBar(webView.url)
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
