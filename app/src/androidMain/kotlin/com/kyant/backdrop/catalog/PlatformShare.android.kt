package com.kyant.backdrop.catalog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

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
