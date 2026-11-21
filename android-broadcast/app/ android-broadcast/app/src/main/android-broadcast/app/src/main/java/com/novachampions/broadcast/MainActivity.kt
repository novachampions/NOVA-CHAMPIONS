
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

        webView.webViewClient = WebViewClient()
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
