package com.lex.app.platform

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ScanUtils {
    fun newCaptureUri(context: Context): Uri {
        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
