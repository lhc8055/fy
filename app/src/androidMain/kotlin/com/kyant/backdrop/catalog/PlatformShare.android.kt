package com.kyant.backdrop.catalog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
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
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.security.MessageDigest

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

@Composable
actual fun SeyraPreloadRemoteImages(urls: List<String>) {
    val context = LocalContext.current
    LaunchedEffect(urls) {
        withContext(Dispatchers.IO) {
            urls.forEach { url ->
                runCatching {
                    getCachedRemoteImageFile(context, url)
                }
            }
        }
    }
}

@Composable
actual fun rememberPreloadRemoteImagesAction(): (List<String>) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope) {
        { urls ->
            scope.launch(Dispatchers.IO) {
                urls.forEach { url ->
                    runCatching {
                        getCachedRemoteImageFile(context, url)
                    }
                }
            }
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
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url, maxBitmapSize) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val file = getCachedRemoteImageFile(context, url)
                decodeSampledBitmap(file, maxBitmapSize)
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

private fun getCachedRemoteImageFile(context: Context, url: String): File {
    val cacheDir = File(context.cacheDir, "remote_images").apply { mkdirs() }
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
        inPreferredConfig = Bitmap.Config.RGB_565
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
