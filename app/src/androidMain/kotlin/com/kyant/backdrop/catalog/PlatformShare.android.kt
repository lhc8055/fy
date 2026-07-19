package com.kyant.backdrop.catalog

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberShareAppAction(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            shareCurrentApp(context)
        }
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
