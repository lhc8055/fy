package com.kyant.backdrop.catalog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

private val startupImagePreloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
private val musicHttpClient = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(12, TimeUnit.SECONDS)
    .build()

private val imageLoadProgressState = mutableStateOf(0f)

fun preloadSeyraStartupImages(context: Context) {
    val appContext = context.applicationContext
    startupImagePreloadScope.launch {
        listOf(
            "https://new.cayfpay.cn/upload/c1/0b6d3b2310c2c06508c5eacb3a5221.jpg" to 900,
            "https://new.cayfpay.cn/upload/43/dd11e5da776272c8fa7eebd84b42f1.jpg" to 900,
            "https://new.cayfpay.cn/upload/6e/a51caa736cb797e7d332d4a9836b2d.jpg" to 900,
            "https://new.cayfpay.cn/upload/7c/eb98dce3d508111dd40ef26c46de45.jpg" to 900,
            "https://new.cayfpay.cn/upload/37/99a5019baa95224ce9adaeed14b633.jpg" to 900,
            "https://new.cayfpay.cn/upload/a9/5818a6e4b274c9d0d559e25d060145.jpg" to 900,
            "https://new.cayfpay.cn/upload/39/04409b3d70e56d240e0491e4915e70.jpg" to 900,
            "https://new.cayfpay.cn/upload/15/0a7387cdf0e793e055b325f22e23d0.jpg" to 900,
            "https://new.cayfpay.cn/upload/b6/bfefc86534d9e54cfdb5761749cb02.jpg" to 900,
            "https://new.cayfpay.cn/upload/e4/4e885b1bacdf43ffb2f28030a59a14.jpg" to 256
        ).forEach { (url, maxBitmapSize) ->
            runCatching {
                preloadRemoteImage(appContext, url, maxBitmapSize)
            }
        }
    }
}

@Composable
actual fun rememberShareAppAction(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            shareCurrentApp(context)
        }
    }
}

@Composable
actual fun rememberOpenFeedbackAction(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            val intent = Intent(context, SeyraFeedbackActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
actual fun rememberCopyTextAction(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Seyra", text))
        }
    }
}

actual suspend fun requestXrayResult(message: String): String {
    val encodedMessage = URLEncoder.encode(message, "UTF-8")
    val connection = URL("http://xiaoxieshen.xyz:1314/xray?msg=$encodedMessage")
        .openConnection() as HttpURLConnection
    return try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        stream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

actual suspend fun requestMusicResult(input: String, platform: String): SeyraMusicResult {
    val formBody = FormBody.Builder()
        .add("input", input)
        .add("type", platform)
        .build()
    val request = Request.Builder()
        .url("https://yy.luodian.net.cn/")
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) Chrome/104.0 Mobile Safari/537.36")
        .post(formBody)
        .build()

    return withContext(Dispatchers.IO) {
        musicHttpClient.newCall(request).execute().use { response ->
            val html = response.body?.string().orEmpty()
            if (!response.isSuccessful || html.isBlank()) {
                error("网络请求失败")
            }

            val document = Jsoup.parse(html)
            val audioUrl = document.readElementValue("j-src")
            val name = document.readElementValue("j-name").ifBlank { input }
            val author = document.readElementValue("j-author").ifBlank { "未知歌手" }
            val lyricUrl = document.readElementValue("j-lrc")

            if (audioUrl.isBlank()) {
                error("没有解析到播放链接")
            }

            SeyraMusicResult(
                audioUrl = audioUrl,
                name = name,
                author = author,
                lyricUrl = lyricUrl
            )
        }
    }
}

private fun org.jsoup.nodes.Document.readElementValue(id: String): String {
    val element = getElementById(id) ?: return ""
    return listOf(
        element.attr("value"),
        element.attr("href"),
        element.attr("src"),
        element.text(),
        element.wholeText()
    ).firstOrNull { it.isNotBlank() }.orEmpty().trim()
}

@Composable
actual fun rememberSeyraMusicPlayerController(): SeyraMusicPlayerController {
    val context = LocalContext.current
    val player = remember(context) {
        ExoPlayer.Builder(context).build()
    }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    return remember(player) {
        object : SeyraMusicPlayerController {
            override val isPlaying: Boolean
                get() = isPlaying

            override fun play(url: String) {
                player.setMediaItem(MediaItem.fromUri(url))
                player.prepare()
                player.play()
                isPlaying = true
            }

            override fun pause() {
                player.pause()
                isPlaying = false
            }

            override fun resume() {
                player.play()
                isPlaying = true
            }
        }
    }
}

@Composable
actual fun rememberShowToastAction(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
actual fun SeyraEmbeddedWebPage(
    url: String,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.textZoom = 92
                settings.mediaPlaybackRequiresUserGesture = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.allowFileAccess = true
                settings.databaseEnabled = true
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}

@Composable
actual fun SeyraPreloadRemoteImages(requests: List<Pair<String, Int>>) {
    val context = LocalContext.current
    LaunchedEffect(requests) {
        val total = requests.size.toFloat()
        withContext(Dispatchers.IO) {
            val jobs = requests.map { (url, maxBitmapSize) ->
                async {
                    runCatching {
                        preloadRemoteImage(context, url, maxBitmapSize)
                    }
                }
            }
            jobs.forEachIndexed { index, deferred ->
                deferred.await()
                imageLoadProgressState.value = (index + 1) / total
            }
        }
    }
}

@Composable
actual fun rememberPreloadRemoteImagesAction(): (List<Pair<String, Int>>) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope) {
        { requests ->
            scope.launch(Dispatchers.IO) {
                requests.forEach { (url, maxBitmapSize) ->
                    runCatching {
                        preloadRemoteImage(context, url, maxBitmapSize)
                    }
                }
            }
        }
    }
}

@Composable
actual fun rememberImageLoadProgress(): Float {
    return imageLoadProgressState.value
}

@Composable
actual fun rememberResetImageLoadProgress(): () -> Unit {
    return remember {
        {
            imageLoadProgressState.value = 0f
        }
    }
}

@Composable
actual fun SeyraRemoteImage(
    url: String,
    maxBitmapSize: Int,
    modifier: Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(url, maxBitmapSize) {
        mutableStateOf(getMemoryCachedBitmap(url, maxBitmapSize))
    }

    LaunchedEffect(url, maxBitmapSize) {
        if (bitmap == null) {
            bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    preloadRemoteImage(context, url, maxBitmapSize)
                    getMemoryCachedBitmap(url, maxBitmapSize)
                }.getOrNull()
            }
        }
    }

    val imageBitmap = bitmap
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier)
    }
}

@Composable
actual fun SeyraNoCacheRemoteImage(
    url: String,
    modifier: Modifier
) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 8_000
                    connection.readTimeout = 8_000
                    connection.useCaches = false
                    connection.inputStream.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                } finally {
                    connection.disconnect()
                }
            }.getOrNull()
        }
    }

    val imageBitmap = bitmap
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier)
    }
}

@Composable
actual fun SeyraSplashImage(modifier: Modifier) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        bitmap = withContext(Dispatchers.IO) {
            loadLocalSplashImage(context)
                ?: runCatching { loadRemoteSplashImage(context) }.getOrNull()
        }
    }

    AnimatedVisibility(
        visible = bitmap != null,
        enter = fadeIn(tween(700, easing = FastOutSlowInEasing)) +
                slideInVertically(tween(700, easing = FastOutSlowInEasing)) { it / 6 }
    ) {
        val imageBitmap = bitmap
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    }
}

actual fun checkAndUpdateSplashImage(context: Any?) {
    val ctx = context as Context
    val prefs = ctx.getSharedPreferences("splash", Context.MODE_PRIVATE)
    val savedVersion = prefs.getString("version", "") ?: ""
    val isFirstLaunch = savedVersion.isEmpty()

    val versionUrl = "https://raw.githubusercontent.com/lhc8055/fy/main/assets/splash/splash_version.json"
    val remoteVersionJson = runCatching {
        val connection = URL(versionUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = if (isFirstLaunch) 15_000 else 8_000
            connection.readTimeout = if (isFirstLaunch) 15_000 else 8_000
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull() ?: return

    val versionMatch = Regex("\"version\"\\s*:\\s*\"([^\"]+)\"").find(remoteVersionJson)
    val remoteVersion = versionMatch?.groupValues?.get(1) ?: return

    val urlMatch = Regex("\"url\"\\s*:\\s*\"([^\"]+)\"").find(remoteVersionJson)
    val imageUrl = urlMatch?.groupValues?.get(1) ?: return

    val cacheFile = File(ctx.filesDir, "splash_image.jpg")

    if (isFirstLaunch || remoteVersion != savedVersion || !cacheFile.exists()) {
        runCatching {
            val connection = URL(imageUrl).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.inputStream.use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                prefs.edit().putString("version", remoteVersion).apply()
            } finally {
                connection.disconnect()
            }
        }
    }

}

private fun loadLocalSplashImage(context: Context): Bitmap? {
    val cacheFile = File(context.filesDir, "splash_image.jpg")
    return if (cacheFile.exists() && cacheFile.length() > 0) {
        BitmapFactory.decodeFile(cacheFile.absolutePath)
    } else {
        null
    }
}

private fun loadRemoteSplashImage(context: Context): Bitmap? {
    val versionUrl = "https://raw.githubusercontent.com/lhc8055/fy/main/assets/splash/splash_version.json"
    val remoteVersionJson = runCatching {
        val connection = URL(versionUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 12_000
            connection.readTimeout = 12_000
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull() ?: return null

    val urlMatch = Regex("\"url\"\s*:\s*\"([^\"]+)\"").find(remoteVersionJson)
    val imageUrl = urlMatch?.groupValues?.get(1) ?: return null

    val connection = URL(imageUrl).openConnection() as HttpURLConnection
    return try {
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.inputStream.use { input ->
            BitmapFactory.decodeStream(input)
        }
    } finally {
        connection.disconnect()
    }
}

@Composable
actual fun LaunchSplashImageUpdater() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            checkAndUpdateSplashImage(context)
        }
        while (true) {
            delay(60_000L)
            withContext(Dispatchers.IO) {
                checkAndUpdateSplashImage(context)
            }
        }
    }
}

private const val maxMemoryCacheEntries = 8
private val remoteBitmapMemoryCache = object : LinkedHashMap<String, Bitmap>(8, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
        return size > maxMemoryCacheEntries
    }
}

private fun preloadRemoteImage(context: Context, url: String, maxBitmapSize: Int) {
    if (getMemoryCachedBitmap(url, maxBitmapSize) != null) {
        return
    }
    val file = getCachedRemoteImageFile(context, url)
    val bitmap = decodeSampledBitmap(file, maxBitmapSize)
    if (bitmap != null) {
        putMemoryCachedBitmap(url, maxBitmapSize, bitmap)
    }
}

private fun getMemoryCachedBitmap(url: String, maxBitmapSize: Int): Bitmap? {
    return synchronized(remoteBitmapMemoryCache) {
        remoteBitmapMemoryCache[memoryCacheKey(url, maxBitmapSize)]
    }
}

private fun putMemoryCachedBitmap(url: String, maxBitmapSize: Int, bitmap: Bitmap) {
    synchronized(remoteBitmapMemoryCache) {
        remoteBitmapMemoryCache[memoryCacheKey(url, maxBitmapSize)] = bitmap
    }
}

private fun memoryCacheKey(url: String, maxBitmapSize: Int): String {
    return "${sha256(url)}_$maxBitmapSize"
}

private fun getCachedRemoteImageFile(context: Context, url: String): File {
    val cacheDir = File(context.filesDir, "remote_images").apply { mkdirs() }
    val cacheFile = File(cacheDir, "${sha256(url)}.img")
    if (cacheFile.exists() && cacheFile.length() > 0L) {
        return cacheFile
    }

    val tempFile = File(cacheDir, "${cacheFile.name}.tmp")
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile.renameTo(cacheFile)
    } finally {
        connection.disconnect()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
    return cacheFile
}

private fun decodeSampledBitmap(file: File, maxBitmapSize: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        return null
    }

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxBitmapSize || bounds.outHeight / sampleSize > maxBitmapSize) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    }
    return BitmapFactory.decodeFile(file.absolutePath, options)
}

private fun sha256(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
    return digest.joinToString("") { byte -> "%02x".format(byte) }
}

private fun shareCurrentApp(context: Context) {
    val sourceApk = File(context.applicationInfo.sourceDir)
    val shareDir = File(context.cacheDir, "shared")
    shareDir.mkdirs()

    val appName = context.getString(context.applicationInfo.labelRes)
    val shareApk = File(shareDir, "$appName.apk")
    sourceApk.copyTo(shareApk, overwrite = true)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        shareApk
    )

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.android.package-archive"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(sendIntent, "分享软件").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(chooser)
}
