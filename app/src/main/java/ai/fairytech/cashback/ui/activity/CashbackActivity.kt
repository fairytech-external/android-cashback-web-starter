package ai.fairytech.cashback.ui.activity

import ai.fairytech.cashback.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class CashbackActivity: AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cashback)

        webView = findViewById<WebView>(R.id.webview).apply {
            settings.textZoom = 100
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(CashbackJsBridge(), "fairyCashbackBridge")

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    if (request == null) {
                        return false
                    }
                    // Cashback web 화면은 웹뷰 내에서 보여주도록 처리
                    if (request.url.host.equals(FAIRY_CASHBACK_DOMAIN)) {
                        return false
                    }
                    // 다른 도메인으로의 리디렉션은 외부 브라우저로 열도록 처리
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.url.toString()))
                    startActivity(intent)
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // localStorage에 쿠키 저장해서 다음에 앱을 열 때 쿠키를 유지하도록 함
                    cookieManager.flush()
                }
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        finish()
                    }
                }
            }
        )

        val redirectTo = intent?.data?.getQueryParameter("redirect_to")
        loadWebView(redirectTo)
    }

    private fun getUserId(): String {
        return  applicationContext.getSharedPreferences("cashback_prefs", MODE_PRIVATE)
            .getString("user_id", "") ?: ""
    }

    private fun loadWebView(redirectTo: String? = null) {
        // TODO: Replace with your actual project ID and API key
        val headers = mapOf(
            "x-moment-project-id" to "<YOUR_PROJECT_ID>",
            "x-moment-web-api-key" to "<YOUR_API_KEY>",
            "x-moment-user-id" to getUserId(),
            "x-moment-platform" to "ANDROID",
        )
        val url = buildString {
            append("https://$FAIRY_CASHBACK_DOMAIN/$FAIRY_CASHBACK_PATH")
            redirectTo?.let { append("?redirect_to=$it") }
        }
        webView.loadUrl(url, headers)
    }

    override fun onDestroy() {
        webView.apply {
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }

    inner class CashbackJsBridge {
        @JavascriptInterface
        fun reload(redirectTo: String) {
            runOnUiThread {
                loadWebView(redirectTo)
            }
        }

        @JavascriptInterface
        fun finish() {
            this@CashbackActivity.finish()
        }
    }

    companion object {
        private const val FAIRY_CASHBACK_DOMAIN = "cashback-ui.moment.fairytech.ai"
        private const val FAIRY_CASHBACK_PATH = "/main"
    }
}