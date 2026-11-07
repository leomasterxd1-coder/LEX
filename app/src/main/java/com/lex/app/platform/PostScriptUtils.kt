package com.lex.app.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.lex.app.data.PrintSettings
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object PostScriptUtils {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 6f
    private const val MAX_PDF_PAGES = 30

    fun fromUris(context: Context, uris: List<android.net.Uri>, settings: PrintSettings): File {
        val bitmaps = uris.mapNotNull { PdfUtils.decodeSampledBitmap(context, it) }
        return fromBitmaps(context, bitmaps, settings)
    }

    fun fromPdf(context: Context, pdf: File, settings: PrintSettings): File {
        return fromBitmaps(context, renderPdf(pdf), settings)
    }

    fun fromText(context: Context, text: String, settings: PrintSettings): File {
        val output = File(context.cacheDir, "lex_text_${System.currentTimeMillis()}.ps")
        FileOutputStream(output).bufferedWriter(Charsets.US_ASCII).use { writer ->
            writer.write("%!PS-Adobe-3.0\n")
            writer.write(pageDevice(settings))
            writer.write("/Courier findfont 10 scalefont setfont\n")
            val lines = wrapLines(text, 88)
            var page = 1
            var y = PAGE_HEIGHT - 24
            writer.write("%%Page: $page $page\n")
            lines.forEach { line ->
                if (y < 24) {
                    writer.write("showpage\n")
                    page++
                    writer.write("%%Page: $page $page\n")
                    y = PAGE_HEIGHT - 24
                }
                writer.write("24 $y moveto (${escape(line)}) show\n")
                y -= 12
            }
            writer.write("showpage\n%%EOF\n")
        }
        return output
    }

    fun fromBitmap(context: Context, bitmap: Bitmap, settings: PrintSettings): File {
        return fromBitmaps(context, listOf(bitmap), settings)
    }

    private fun fromBitmaps(context: Context, bitmaps: List<Bitmap>, settings: PrintSettings): File {
        require(bitmaps.isNotEmpty()) { "Nenhuma página disponível para impressão." }
        val output = File(context.cacheDir, "lex_images_${System.currentTimeMillis()}.ps")
        FileOutputStream(output).use { out ->
            writeAscii(out, "%!PS-Adobe-3.0\n")
            writeAscii(out, pageDevice(settings))
            bitmaps.forEachIndexed { index, bitmap ->
                writeAscii(out, "%%Page: ${index + 1} ${index + 1}\n")
                writeImagePage(out, bitmap)
                bitmap.recycle()
            }
            writeAscii(out, "%%EOF\n")
        }
        return output
    }

    private fun writeImagePage(out: FileOutputStream, bitmap: Bitmap) {
        val availableWidth = PAGE_WIDTH - MARGIN * 2
        val availableHeight = PAGE_HEIGHT - MARGIN * 2
        val scale = min(availableWidth / bitmap.width, availableHeight / bitmap.height)
        val drawWidth = bitmap.width * scale
        val drawHeight = bitmap.height * scale
        val x = MARGIN
        val y = (PAGE_HEIGHT - drawHeight) / 2f

        val jpeg = ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it)
        }.toByteArray()

        writeAscii(out, "gsave\n")
        writeAscii(out, "${format(x)} ${format(y)} translate\n")
        writeAscii(out, "${format(drawWidth)} ${format(drawHeight)} scale\n")
        writeAscii(out, "/DeviceRGB setcolorspace\n")
        writeAscii(
            out,
            "<< /ImageType 1 /Width ${bitmap.width} /Height ${bitmap.height} " +
                "/BitsPerComponent 8 /Decode [0 1 0 1 0 1] " +
                "/ImageMatrix [${bitmap.width} 0 0 -${bitmap.height} 0 ${bitmap.height}] " +
                "/DataSource currentfile /DCTDecode filter >> image\n"
        )
        out.write(jpeg)
        writeAscii(out, "\ngrestore\nshowpage\n")
    }

    private fun renderPdf(pdf: File): List<Bitmap> {
        val pages = mutableListOf<Bitmap>()
        val descriptor = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)
        try {
            PdfRenderer(descriptor).use { renderer ->
                val count = min(renderer.pageCount, MAX_PDF_PAGES)
                for (index in 0 until count) {
                    val page = renderer.openPage(index)
                    try {
                        val width = 1240
                        val height = (width * page.height / page.width.toFloat()).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        pages += bitmap
                    } finally {
                        page.close()
                    }
                }
            }
        } finally {
            descriptor.close()
        }
        return pages
    }

    private fun pageDevice(settings: PrintSettings): String {
        val copies = settings.copies.coerceIn(1, 999)
        return "<< /PageSize [$PAGE_WIDTH $PAGE_HEIGHT] /NumCopies $copies >> setpagedevice\n"
    }

    private fun wrapLines(text: String, width: Int): List<String> {
        val result = mutableListOf<String>()
        text.lineSequence().forEach { raw ->
            val safe = raw.replace('\t', ' ')
            if (safe.length <= width) {
                result += safe
            } else {
                var start = 0
                while (start < safe.length) {
                    val end = min(start + width, safe.length)
                    result += safe.substring(start, end)
                    start = end
                }
            }
        }
        return result.ifEmpty { listOf("") }
    }

    private fun escape(value: String): String = buildString {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '(' -> append("\\(")
                ')' -> append("\\)")
                else -> if (char.code in 32..126) append(char) else append('?')
            }
        }
    }

    private fun writeAscii(out: FileOutputStream, value: String) {
        out.write(value.toByteArray(Charsets.US_ASCII))
    }

    private fun format(value: Float): String = String.format(java.util.Locale.US, "%.2f", value)
}
