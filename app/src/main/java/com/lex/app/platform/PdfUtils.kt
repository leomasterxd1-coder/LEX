package com.lex.app.platform

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object PdfUtils {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun displayName(context: Context, uri: Uri): String {
        if (uri.scheme == "file") return uri.lastPathSegment ?: "Document"
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        }.getOrNull() ?: uri.lastPathSegment ?: "Document"
    }

    fun sizeLabel(context: Context, uri: Uri): String {
        val size = runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
                }
        }.getOrNull()
        return when {
            size == null -> ""
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format(Locale.US, "%.1f MB", size / (1024.0 * 1024.0))
        }
    }

    fun pdfFromUris(context: Context, uris: List<Uri>): File {
        val output = File(context.cacheDir, "lex_print_${System.currentTimeMillis()}.pdf")
        val document = PdfDocument()
        try {
            uris.forEachIndexed { index, uri ->
                val bitmap = decodeSampledBitmap(context, uri) ?: return@forEachIndexed
                val page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                )
                drawBitmapOnPage(page.canvas, bitmap)
                document.finishPage(page)
                bitmap.recycle()
            }
            if (document.pages.isEmpty()) error("No pages could be created from the selected files.")
            FileOutputStream(output).use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return output
    }

    fun pdfFromText(context: Context, text: String): Pair<File, Int> {
        val output = File(context.cacheDir, "lex_text_${System.currentTimeMillis()}.pdf")
        val document = PdfDocument()
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.MONOSPACE
        }
        val margin = 40f
        val lineHeight = 15f
        val maxWidth = PAGE_WIDTH - margin * 2
        val lines = buildList {
            text.lineSequence().forEach { raw ->
                if (raw.isEmpty()) {
                    add("")
                } else {
                    var remaining = raw
                    while (remaining.isNotEmpty()) {
                        val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
                        add(remaining.take(count))
                        remaining = remaining.drop(count)
                    }
                }
            }
        }.ifEmpty { listOf("") }

        var pageNumber = 1
        var lineIndex = 0
        try {
            while (lineIndex < lines.size && pageNumber <= 200) {
                val page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                page.canvas.drawColor(Color.WHITE)
                var y = margin + lineHeight
                while (lineIndex < lines.size && y < PAGE_HEIGHT - margin) {
                    page.canvas.drawText(lines[lineIndex], margin, y, paint)
                    lineIndex++
                    y += lineHeight
                }
                document.finishPage(page)
                pageNumber++
            }
            FileOutputStream(output).use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return output to (pageNumber - 1).coerceAtLeast(1)
    }

    fun pdfFromImageBytes(context: Context, bytes: ByteArray): File {
        val output = File(context.cacheDir, "lex_image_${System.currentTimeMillis()}.pdf")
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Could not read the image.")
        val document = PdfDocument()
        try {
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            )
            drawBitmapOnPage(page.canvas, bitmap)
            document.finishPage(page)
            FileOutputStream(output).use { document.writeTo(it) }
        } finally {
            document.close()
            bitmap.recycle()
        }
        return output
    }

    fun createSimulatedScan(context: Context): Uri {
        val bitmap = Bitmap.createBitmap(1240, 1754, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val red = Paint().apply { color = Color.rgb(200, 16, 46); isAntiAlias = true }
        canvas.drawRect(0f, 0f, 1240f, 120f, red)
        val white = Paint().apply { color = Color.WHITE; textSize = 48f; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("LEX", 60f, 78f, white)
        val dark = Paint().apply { color = Color.rgb(40, 40, 40); textSize = 38f; isAntiAlias = true }
        canvas.drawText("Simulated scan", 60f, 230f, dark)
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(date, 60f, 290f, dark)
        val line = Paint().apply { color = Color.rgb(90, 90, 90); strokeWidth = 3f; isAntiAlias = true }
        var y = 360f
        repeat(24) { index ->
            val length = if (index % 6 == 5) 500f else 1050f
            canvas.drawLine(60f, y, 60f + length, y, line)
            y += 42f
        }
        canvas.drawRect(60f, 1420f, 1180f, 1660f, Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.rgb(200, 16, 46)
        })
        val caption = Paint().apply { color = Color.rgb(120, 120, 120); textSize = 30f; isAntiAlias = true }
        canvas.drawText("Place your document on the scanner glass.", 75f, 1550f, caption)

        val dir = File(context.cacheDir, "scans").apply { mkdirs() }
        val file = File(dir, "scan_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun savePdfToDownloads(context: Context, pdf: File, displayName: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LEX")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = runCatching {
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        }.getOrNull() ?: return null
        runCatching {
            resolver.openOutputStream(uri)?.use { output ->
                pdf.inputStream().use { input -> input.copyTo(output) }
            }
        }.onFailure {
            resolver.delete(uri, null, null)
            return null
        }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    fun decodeSampledBitmap(context: Context, uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        }
        val sample = calculateSampleSize(bounds.outWidth, bounds.outHeight, 2200)
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        }.getOrNull()
    }

    private fun calculateSampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        var w = max(width, 1)
        var h = max(height, 1)
        while (w / 2 >= target || h / 2 >= target) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun drawBitmapOnPage(canvas: Canvas, bitmap: Bitmap) {
        canvas.drawColor(Color.WHITE)
        val margin = 24f
        val availableWidth = PAGE_WIDTH - margin * 2
        val availableHeight = PAGE_HEIGHT - margin * 2
        val scale = minOf(availableWidth / bitmap.width, availableHeight / bitmap.height)
        val drawWidth = bitmap.width * scale
        val drawHeight = bitmap.height * scale
        val left = (PAGE_WIDTH - drawWidth) / 2f
        val top = (PAGE_HEIGHT - drawHeight) / 2f
        canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + drawWidth, top + drawHeight), null)
    }
}
