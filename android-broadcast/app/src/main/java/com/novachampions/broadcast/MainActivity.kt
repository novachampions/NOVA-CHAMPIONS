
package com.novachampions.broadcast

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import io.livekit.android.room.track.screencapture.ScreenCaptureParams
import kotlinx.coroutines.launch
import android.net.Uri
import android.webkit.WebResourceRequest

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var liveKitRoom: Room? = null
    private var pendingServerUrl = ""
    private var pendingToken = ""

    private val screenCaptureLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val data: Intent = result.data ?: return@registerForActivityResult

            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                try {
                    liveKitRoom?.localParticipant?.setScreenShareEnabled(
                        true,
                        ScreenCaptureParams(data)
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "بدأ بث الشاشة",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "تعذر بدء البث: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient = object : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest
    ): Boolean {
        return handleExternalLink(request.url.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        url: String?
    ): Boolean {
        return handleExternalLink(url ?: return false)
    }
        }
        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(
            NovaAndroidBridge(this),
            "NovaAndroid"
        )

        setContentView(webView)

        webView.loadUrl(
            "https://novachampions.github.io/NOVA-CHAMPIONS/"
        )
    }

    private fun handleExternalLink(url: String): Boolean {
        val uri = Uri.parse(url)
        val scheme = uri.scheme ?: return false
        val host = uri.host ?: ""

        val isWhatsApp =
            scheme.equals("whatsapp", true) ||
            host.equals("api.whatsapp.com", true) ||
            host.equals("wa.me", true) ||
            host.equals("www.whatsapp.com", true)

        if (!isWhatsApp) return false

        return try {
    val intent = Intent(Intent.ACTION_VIEW, uri)

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://api.whatsapp.com/send?phone=" +
                uri.getQueryParameter("phone").orEmpty())
        )
        startActivity(browserIntent)
    }
    true
} catch (e: Exception) {
    Toast.makeText(
        this,
        "تعذر فتح WhatsApp",
        Toast.LENGTH_LONG
    ).show()
    true
}
            Toast.makeText(
                this,
                "تعذر فتح WhatsApp. تأكد من تثبيته.",
                Toast.LENGTH_LONG
            ).show()
            true
        }
    }
    
    fun startScreenBroadcast(serverUrl: String, token: String) {
        pendingServerUrl = serverUrl
        pendingToken = token

        if (pendingServerUrl.isBlank() || pendingToken.isBlank()) {
            Toast.makeText(this, "معلومات الاتصال ناقصة", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                liveKitRoom = LiveKit.create(applicationContext)

                liveKitRoom?.connect(
                    pendingServerUrl,
                    pendingToken
                )

                val manager = getSystemService(
                    MEDIA_PROJECTION_SERVICE
                ) as MediaProjectionManager

                screenCaptureLauncher.launch(
                    manager.createScreenCaptureIntent()
                )

            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "فشل الاتصال: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
fun stopScreenBroadcast() {
    lifecycleScope.launch {
        try {
            liveKitRoom?.localParticipant?.setScreenShareEnabled(false)
            liveKitRoom?.disconnect()
            liveKitRoom = null

            Toast.makeText(
                this@MainActivity,
                "تم إيقاف البث",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                this@MainActivity,
                "تعذر إيقاف البث: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
