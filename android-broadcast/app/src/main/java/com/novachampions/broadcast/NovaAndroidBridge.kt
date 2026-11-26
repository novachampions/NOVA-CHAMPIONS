
package com.novachampions.broadcast

import android.webkit.JavascriptInterface

class NovaAndroidBridge(private val activity: MainActivity) {

    @JavascriptInterface
    fun startScreenBroadcast(serverUrl: String, token: String) {
        activity.runOnUiThread {
            activity.startScreenBroadcast(serverUrl, token)
        }
    }

    @JavascriptInterface
    fun stopScreenBroadcast() {
        activity.runOnUiThread {
            activity.stopScreenBroadcast()
        }
    }

    @JavascriptInterface
    fun setBannerVisible(visible: Boolean) {
        activity.setBannerVisible(visible)
    }
}
