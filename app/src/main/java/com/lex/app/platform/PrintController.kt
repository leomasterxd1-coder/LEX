package com.lex.app.platform

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.webkit.WebView
import com.lex.app.data.PrintSettings
import java.io.File
import java.io.FileOutputStream

object PrintController {

    fun printUri(
        context: Context,
        uri: Uri,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = runCatching {
        val (pdf, pageCount) = preparePdf(context, uri)
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = PdfFilePrintAdapter(pdf, jobName, pageCount)
        manager.print(jobName, adapter, attributes(settings))
    }

    fun printUris(
        context: Context,
        uris: List<Uri>,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = runCatching {
        require(uris.isNotEmpty()) { "Selecione pelo menos uma página." }
        val pdf = PdfUtils.pdfFromUris(context, uris)
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = PdfFilePrintAdapter(pdf, jobName, uris.size)
        manager.print(jobName, adapter, attributes(settings))
    }

    fun printText(
        context: Context,
        text: String,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = runCatching {
        val (pdf, pages) = PdfUtils.pdfFromText(context, text)
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = PdfFilePrintAdapter(pdf, jobName, pages)
        manager.print(jobName, adapter, attributes(settings))
    }

    fun printWebView(
        context: Context,
        webView: WebView,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = runCatching {
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        manager.print(jobName, webView.createPrintDocumentAdapter(jobName), attributes(settings))
    }

    private fun preparePdf(context: Context, uri: Uri): Pair<File, Int> {
        val mime = context.contentResolver.getType(uri).orEmpty()
        val name = PdfUtils.displayName(context, uri).lowercase()
        return when {
            mime.startsWith("image/") || name.endsWith(".png") || name.endsWith(".jpg") ||
                name.endsWith(".jpeg") || name.endsWith(".webp") -> {
                PdfUtils.pdfFromUris(context, listOf(uri)) to 1
            }

            mime == "application/pdf" || name.endsWith(".pdf") -> {
                val file = File(context.cacheDir, "lex_pdf_${System.currentTimeMillis()}.pdf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                } ?: error("Não foi possível abrir o PDF selecionado.")
                file to PrintDocumentInfo.PAGE_COUNT_UNKNOWN
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
                PdfUtils.pdfFromText(context, text)
            }

            else -> error("Este tipo de arquivo não é compatível com impressão direta.")
        }
    }

    private fun attributes(settings: PrintSettings): PrintAttributes {
        val media = when (settings.mediaSize.lowercase()) {
            "letter", "carta" -> PrintAttributes.MediaSize.NA_LETTER
            "legal", "ofício", "oficio" -> PrintAttributes.MediaSize.NA_LEGAL
            "a3" -> PrintAttributes.MediaSize.ISO_A3
            "a5" -> PrintAttributes.MediaSize.ISO_A5
            "a6" -> PrintAttributes.MediaSize.ISO_A6
            else -> PrintAttributes.MediaSize.ISO_A4
        }
        val oriented = if (settings.orientation.equals("Paisagem", ignoreCase = true) || settings.orientation.equals("Landscape", ignoreCase = true)) {
            media.asLandscape()
        } else {
            media.asPortrait()
        }
        return PrintAttributes.Builder()
            .setMediaSize(oriented)
            .setColorMode(
                if (settings.color) PrintAttributes.COLOR_MODE_COLOR
                else PrintAttributes.COLOR_MODE_MONOCHROME
            )
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
    }

    private class PdfFilePrintAdapter(
        private val file: File,
        private val jobName: String,
        private val pageCount: Int
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(if (pageCount > 0) pageCount else PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
                return
            }
            if (destination == null) {
                callback.onWriteFailed("Nenhum destino foi fornecido.")
                return
            }
            runCatching {
                java.io.FileInputStream(file).use { input ->
                    ParcelFileDescriptor.AutoCloseOutputStream(destination).use { output ->
                        input.copyTo(output)
                    }
                }
            }.onSuccess {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }.onFailure {
                callback.onWriteFailed(it.message ?: "Não foi possível gravar o trabalho de impressão.")
            }
        }

        override fun onFinish() {
            file.delete()
        }
    }
}
