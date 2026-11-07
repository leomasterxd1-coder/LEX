package com.lex.app.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.webkit.WebView
import com.lex.app.data.Device
import com.lex.app.data.PrintSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PrintController {

    suspend fun printUri(
        context: Context,
        uri: Uri,
        device: Device,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = runCatching {
        val payload = withContext(Dispatchers.IO) { preparePayload(context, uri, settings) }
        DirectPrinterClient.send(
            device,
            payload.pdf,
            payload.postScript,
            jobName,
            settings
        ).getOrThrow()
    }

    suspend fun printUris(
        context: Context,
        uris: List<Uri>,
        device: Device,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = runCatching {
        require(uris.isNotEmpty()) { "Selecione pelo menos uma página." }
        val pdf = withContext(Dispatchers.IO) { PdfUtils.pdfFromUris(context, uris) }
        val postScript = withContext(Dispatchers.IO) {
            runCatching { PostScriptUtils.fromUris(context, uris, settings) }.getOrNull()
        }
        DirectPrinterClient.send(device, pdf, postScript, jobName, settings).getOrThrow()
    }

    suspend fun printText(
        context: Context,
        text: String,
        device: Device,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = runCatching {
        val pdf = withContext(Dispatchers.IO) { PdfUtils.pdfFromText(context, text).first }
        val postScript = withContext(Dispatchers.IO) {
            runCatching { PostScriptUtils.fromText(context, text, settings) }.getOrNull()
        }
        DirectPrinterClient.send(device, pdf, postScript, jobName, settings).getOrThrow()
    }

    @Suppress("DEPRECATION")
    suspend fun printWebView(
        context: Context,
        webView: WebView,
        device: Device,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> {
        val bitmap = withContext(Dispatchers.Main) {
            val picture = webView.capturePicture()
                ?: error("A página da web ainda não terminou de carregar.")
            val width = picture.width.coerceAtLeast(webView.width).coerceAtLeast(1)
            val height = picture.height
                .coerceAtLeast(webView.height)
                .coerceAtLeast(1)
                .coerceAtMost(20_000)
            val captured = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(captured)
            canvas.drawColor(android.graphics.Color.WHITE)
            picture.draw(canvas)
            captured
        }
        return runCatching {
            val pdf = withContext(Dispatchers.IO) { PdfUtils.pdfFromBitmap(context, bitmap) }
            val postScript = withContext(Dispatchers.IO) {
                runCatching { PostScriptUtils.fromBitmap(context, bitmap, settings) }.getOrNull()
            }
            DirectPrinterClient.send(device, pdf, postScript, jobName, settings).getOrThrow()
        }.also {
            bitmap.recycle()
        }
    }

    private data class Payload(val pdf: File?, val postScript: File?)

    private fun preparePayload(context: Context, uri: Uri, settings: PrintSettings): Payload {
        val mime = context.contentResolver.getType(uri).orEmpty()
        val name = PdfUtils.displayName(context, uri).lowercase()
        return when {
            mime.startsWith("image/") || name.endsWith(".png") || name.endsWith(".jpg") ||
                name.endsWith(".jpeg") || name.endsWith(".webp") -> {
                Payload(
                    pdf = PdfUtils.pdfFromUris(context, listOf(uri)),
                    postScript = runCatching {
                        PostScriptUtils.fromUris(context, listOf(uri), settings)
                    }.getOrNull()
                )
            }

            mime == "application/pdf" || name.endsWith(".pdf") -> {
                val file = File(context.cacheDir, "lex_pdf_${System.currentTimeMillis()}.pdf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                } ?: error("Não foi possível abrir o PDF selecionado.")
                Payload(
                    pdf = file,
                    postScript = runCatching {
                        PostScriptUtils.fromPdf(context, file, settings)
                    }.getOrNull()
                )
            }

            mime.startsWith("text/") || name.endsWith(".txt") || name.endsWith(".csv") ||
                name.endsWith(".md") || name.endsWith(".json") || name.endsWith(".xml") -> {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    val builder = StringBuilder()
                    var total = 0
                    reader.forEachLine { line ->
                        if (total < 1_000_000) {
                            builder.appendLine(line)
                            total += line.length
                        }
                    }
                    builder.toString()
                } ?: error("Não foi possível ler o arquivo selecionado.")
                Payload(
                    pdf = PdfUtils.pdfFromText(context, text).first,
                    postScript = runCatching {
                        PostScriptUtils.fromText(context, text, settings)
                    }.getOrNull()
                )
            }

            else -> error("Este tipo de arquivo não é compatível com impressão direta.")
        }
    }
}
