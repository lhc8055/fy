package com.so.movie.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * 视频源嗅探器 — 使用 WebView 拦截网页中的视频流地址
 * 支持 m3u8 (HLS) 和 mp4 直链检测
 */
class VideoSourceSniffer(private val context: Context) {

    companion object {
        private const val SNIFF_TIMEOUT = 15000L // 15秒超时
        private val VIDEO_EXTENSIONS = listOf(".m3u8", ".mp4", ".flv", ".avi", ".mkv", ".ts")
        private val VIDEO_CONTENT_TYPES = listOf("video/", "application/x-mpegURL", "application/vnd.apple.mpegurl")
    }

    /**
     * 检测 URL 是否为直接视频链接
     */
    fun isDirectVideoUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return VIDEO_EXTENSIONS.any { lowerUrl.contains(it) } ||
               lowerUrl.contains(".m3u8") ||
               lowerUrl.contains(".mp4")
    }

    /**
     * 嗅探视频流地址
     * @param pageUrl 播放页 URL
     * @param userAgent 自定义 User-Agent
     * @param referer 自定义 Referer
     * @return 视频流地址，null 表示未找到
     */
    suspend fun sniffVideoUrl(
        pageUrl: String,
        userAgent: String = "",
        referer: String = ""
    ): String? = withContext(Dispatchers.Main) {
        // 如果已经是直接视频链接，直接返回
        if (isDirectVideoUrl(pageUrl)) {
            return@withContext pageUrl
        }

        withTimeoutOrNull(SNIFF_TIMEOUT) {
            suspendCancellableCoroutine { cont ->
                val handler = Handler(Looper.getMainLooper())
                var webView: WebView? = null
                var resolved = false

                fun finish(result: String?) {
                    if (resolved) return
                    resolved = true
                    handler.post {
                        webView?.apply {
                            stopLoading()
                            loadUrl("about:blank")
                            destroy()
                        }
                        webView = null
                        if (cont.isActive) cont.resume(result)
                    }
                }

                webView = createWebView(userAgent).apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url?.toString() ?: return null

                            // 检测视频 URL
                            if (isVideoUrl(url)) {
                                finish(url)
                            }
                            return null
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            url: String?
                        ): WebResourceResponse? {
                            // 兼容旧 API
                            if (url != null && isVideoUrl(url)) {
                                finish(url)
                            }
                            return null
                        }

                        @SuppressLint("WebViewClientOnReceivedSslError")
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: android.net.http.SslError?
                        ) {
                            handler?.proceed() // 忽略 SSL 错误
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // 注入 JS 查找 video 标签
                            view?.evaluateJavascript(
                                """
                                (function() {
                                    var videos = document.querySelectorAll('video');
                                    for (var i = 0; i < videos.length; i++) {
                                        var src = videos[i].src || videos[i].currentSrc;
                                        if (src) {
                                            window.AndroidVideoSniffer.onVideoFound(src);
                                        }
                                    }
                                    var sources = document.querySelectorAll('source');
                                    for (var i = 0; i < sources.length; i++) {
                                        var src = sources[i].src;
                                        if (src) {
                                            window.AndroidVideoSniffer.onVideoFound(src);
                                        }
                                    }
                                })();
                                """.trimIndent(),
                                null
                            )
                        }

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?
                        ) {
                            super.onPageStarted(view, url, favicon)
                        }
                    }

                    // 添加 JS 接口
                    addJavascriptInterface(
                        object {
                            @android.webkit.JavascriptInterface
                            fun onVideoFound(url: String) {
                                if (isVideoUrl(url)) {
                                    finish(url)
                                }
                            }
                        },
                        "AndroidVideoSniffer"
                    )

                    // 设置请求头
                    val headers = mutableMapOf<String, String>()
                    if (referer.isNotEmpty()) {
                        headers["Referer"] = referer
                    }

                    loadUrl(pageUrl, headers)
                }

                // 超时处理
                handler.postDelayed({
                    finish(null)
                }, SNIFF_TIMEOUT)

                cont.invokeOnCancellation {
                    finish(null)
                }
            }
        }
    }

    /**
     * 判断 URL 是否为视频流地址
     */
    private fun isVideoUrl(url: String): Boolean {
        val lower = url.lowercase()
        // 排除常见的非视频 URL
        if (lower.contains(".jpg") || lower.contains(".png") || lower.contains(".gif") ||
            lower.contains(".css") || lower.contains(".js") || lower.contains(".woff") ||
            lower.contains(".ico") || lower.contains(".svg")) {
            return false
        }
        // 检查视频扩展名
        if (VIDEO_EXTENSIONS.any { lower.contains(it) }) {
            return true
        }
        // 检查 m3u8 关键词
        if (lower.contains("m3u8")) return true
        if (lower.contains(".mp4")) return true
        return false
    }

    /**
     * 创建 WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(userAgent: String): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                if (userAgent.isNotEmpty()) {
                    userAgentString = userAgent
                }
                // 允许混合内容
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            // 启用硬件加速
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        }
    }
}
