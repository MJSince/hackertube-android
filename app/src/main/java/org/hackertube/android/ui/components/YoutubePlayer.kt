package org.hackertube.android.ui.components

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.hackertube.android.ui.theme.HnOrange

/**
 * Official YouTube privacy-enhanced embed via WebView.
 * Uses youtube-nocookie.com (same approach as hackertube.org).
 * Does not extract streams or bypass platform restrictions.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true
) {
    var loading by remember(videoId) { mutableStateOf(true) }
    var error by remember(videoId) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Custom chrome client for fullscreen
    val chromeClient = remember {
        object : WebChromeClient() {
            private var customView: android.view.View? = null
            private var customViewCallback: CustomViewCallback? = null
            private var originalOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                val act = activity ?: return
                customView = view
                customViewCallback = callback
                originalOrientation = act.requestedOrientation
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                val decor = act.window.decorView as FrameLayout
                decor.addView(
                    view,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }

            override fun onHideCustomView() {
                val act = activity ?: return
                val decor = act.window.decorView as FrameLayout
                customView?.let { decor.removeView(it) }
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                act.requestedOrientation = originalOrientation
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    webChromeClient = chromeClient
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            loading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            errorObj: WebResourceError?
                        ) {
                            if (request?.isForMainFrame == true) {
                                loading = false
                                error = "Unable to load player"
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            // Keep navigation inside the embed when possible
                            val host = request?.url?.host ?: return false
                            return host !in listOf(
                                "www.youtube-nocookie.com",
                                "youtube-nocookie.com",
                                "www.youtube.com",
                                "youtube.com",
                                "i.ytimg.com",
                                "www.youtube-nocookie.com"
                            )
                        }
                    }
                    val ap = if (autoplay) "1" else "0"
                    val html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
                          <style>
                            * { margin:0; padding:0; }
                            html, body { width:100%; height:100%; background:#000; overflow:hidden; }
                            iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:0; }
                          </style>
                        </head>
                        <body>
                          <iframe
                            src="https://www.youtube-nocookie.com/embed/$videoId?rel=0&modestbranding=1&playsinline=1&autoplay=$ap"
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen"
                            allowfullscreen
                            referrerpolicy="strict-origin-when-cross-origin">
                          </iframe>
                        </body>
                        </html>
                    """.trimIndent()
                    loadDataWithBaseURL(
                        "https://www.youtube-nocookie.com",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { webView ->
                // Reload only when videoId changes is handled by recomposition key
            },
            modifier = Modifier.fillMaxSize()
        )

        if (loading && error == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = HnOrange
            )
        }
        if (error != null) {
            Text(
                text = error ?: "Error",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    DisposableEffect(videoId) {
        onDispose {
            // WebView cleanup handled by AndroidView disposal
        }
    }
}
