package com.lex.app.platform

import android.util.Log
import com.lex.app.data.Device
import com.lex.app.data.PrintSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

object DirectPrinterClient {

    suspend fun send(
        device: Device,
        pdf: File?,
        postScript: File?,
        jobName: String,
        settings: PrintSettings
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val rawAddress = device.address.trim()
        if (rawAddress.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Cadastre o endereço IP da impressora antes de imprimir.")
            )
        }
        if (pdf == null && postScript == null) {
            return@withContext Result.failure(
                IllegalStateException("Nenhum documento foi gerado para impressão.")
            )
        }

        val host = parseHost(rawAddress)
        val explicitPort = parsePort(rawAddress)
        if (host.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Endereço IP da impressora inválido.")
            )
        }

        if (explicitPort == RAW_PORT) {
            if (postScript == null) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Esta impressora usa PostScript na porta 9100 e o arquivo não pôde ser convertido."
                    )
                )
            }
            return@withContext runCatching { sendRaw(host, RAW_PORT, postScript) }
                .onFailure { Log.e(TAG, "Falha no envio PostScript para $host:$RAW_PORT", it) }
        }

        val ippPort = explicitPort ?: IPP_PORT
        if (pdf != null) {
            val ippResult = runCatching {
                val errors = mutableListOf<Throwable>()
                for (path in listOf("/ipp/print", "/ipp/printer")) {
                    val attempt = runCatching { sendIpp(host, ippPort, path, pdf, jobName, settings) }
                    if (attempt.isSuccess) return@runCatching
                    attempt.exceptionOrNull()?.let { errors += it }
                }
                throw errors.lastOrNull() ?: IllegalStateException("Falha na impressão IPP.")
            }
            if (ippResult.isSuccess) return@withContext Result.success(Unit)

            if (postScript == null) {
                val failure = ippResult.exceptionOrNull()
                    ?: IllegalStateException("A impressora não aceitou o PDF via IPP.")
                Log.e(TAG, "Falha IPP sem fallback PostScript em $host:$ippPort", failure)
                return@withContext Result.failure(
                    IllegalStateException(
                        "A impressora não aceitou o PDF via IPP e não foi possível convertê-lo para PostScript.",
                        failure
                    )
                )
            }
            Log.w(TAG, "IPP falhou; tentando PostScript na porta $RAW_PORT", ippResult.exceptionOrNull())
        }

        if (postScript != null) {
            val rawResult = runCatching { sendRaw(host, RAW_PORT, postScript) }
            if (rawResult.isSuccess) return@withContext Result.success(Unit)
            val failure = rawResult.exceptionOrNull()
                ?: IllegalStateException("Não foi possível enviar o trabalho para a impressora.")
            Log.e(TAG, "Falha no envio PostScript para $host:$RAW_PORT", failure)
            return@withContext Result.failure(failure)
        }

        Result.failure(IllegalStateException("Não foi possível imprimir em $host:$ippPort."))
    }

    private fun sendIpp(host: String, port: Int, path: String, pdf: File, jobName: String, settings: PrintSettings) {
        val header = buildIppHeader(host, port, path, jobName, settings)
        val connection = URL("http://$host:$port$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 10_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Content-Type", "application/ipp")
            connection.setRequestProperty("Accept", "application/ipp")
            connection.setFixedLengthStreamingMode(header.size + pdf.length())
            connection.outputStream.use { output ->
                output.write(header)
                pdf.inputStream().use { input -> input.copyTo(output, 32 * 1024) }
                output.flush()
            }
            val httpCode = connection.responseCode
            if (httpCode !in 200..299) {
                error("A impressora respondeu HTTP $httpCode em $host:$port.")
            }
            val response = connection.inputStream.use { it.readBytes() }
            if (response.size >= 4) {
                val status = ((response[2].toInt() and 0xFF) shl 8) or (response[3].toInt() and 0xFF)
                if (status != 0x0000) {
                    error("A impressora recusou o trabalho (IPP 0x${status.toString(16)}).")
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun sendRaw(host: String, port: Int, pdf: File) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 10_000)
                socket.soTimeout = 20_000
                socket.getOutputStream().use { output ->
                    pdf.inputStream().use { input -> input.copyTo(output, 32 * 1024) }
                    output.flush()
                }
            }
        } catch (error: Exception) {
            throw IllegalStateException(
                "Não foi possível conectar à impressora em $host:$port. " +
                    "Verifique se ela está ligada e na mesma rede.",
                error
            )
        }
    }

    private fun buildIppHeader(
        host: String,
        port: Int,
        path: String,
        jobName: String,
        settings: PrintSettings
    ): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x01, 0x01))
        out.write(byteArrayOf(0x00, 0x02))
        out.write(byteArrayOf(0x00, 0x00, 0x00, 0x01))

        out.write(0x01)
        writeStringAttribute(out, 0x47, "attributes-charset", "utf-8")
        writeStringAttribute(out, 0x48, "attributes-natural-language", "pt-br")
        writeStringAttribute(out, 0x45, "printer-uri", "ipp://$host:$port$path")
        writeStringAttribute(out, 0x42, "requesting-user-name", "LEX")
        writeStringAttribute(out, 0x42, "job-name", jobName.take(80))
        writeStringAttribute(out, 0x49, "document-format", "application/pdf")

        out.write(0x02)
        writeIntegerAttribute(out, "copies", settings.copies.coerceIn(1, 999))
        writeStringAttribute(
            out,
            0x44,
            "print-color-mode",
            if (settings.color) "color" else "monochrome"
        )
        writeStringAttribute(
            out,
            0x44,
            "sides",
            if (settings.duplex) "two-sided-long-edge" else "one-sided"
        )
        writeStringAttribute(out, 0x44, "media", ippMedia(settings.mediaSize))
        writeIntegerAttribute(
            out,
            "orientation-requested",
            if (settings.orientation.equals("Paisagem", ignoreCase = true)) 4 else 3
        )
        out.write(0x03)
        return out.toByteArray()
    }

    private fun ippMedia(media: String): String = when (media.lowercase()) {
        "carta", "letter" -> "na_letter_8.5x11in"
        "ofício", "oficio", "legal" -> "na_legal_8.5x14in"
        "a3" -> "iso_a3_297x420mm"
        "a5" -> "iso_a5_148x210mm"
        "a6" -> "iso_a6_105x148mm"
        else -> "iso_a4_210x297mm"
    }

    private fun writeStringAttribute(
        out: ByteArrayOutputStream,
        tag: Int,
        name: String,
        value: String
    ) {
        val nameBytes = name.toByteArray(Charsets.UTF_8)
        val valueBytes = value.toByteArray(Charsets.UTF_8)
        out.write(tag)
        out.write((nameBytes.size shr 8) and 0xFF)
        out.write(nameBytes.size and 0xFF)
        out.write(nameBytes)
        out.write((valueBytes.size shr 8) and 0xFF)
        out.write(valueBytes.size and 0xFF)
        out.write(valueBytes)
    }

    private fun writeIntegerAttribute(out: ByteArrayOutputStream, name: String, value: Int) {
        val nameBytes = name.toByteArray(Charsets.UTF_8)
        out.write(0x21)
        out.write((nameBytes.size shr 8) and 0xFF)
        out.write(nameBytes.size and 0xFF)
        out.write(nameBytes)
        out.write(0x00)
        out.write(0x04)
        out.write((value shr 24) and 0xFF)
        out.write((value shr 16) and 0xFF)
        out.write((value shr 8) and 0xFF)
        out.write(value and 0xFF)
    }

    private fun parseHost(address: String): String =
        normalize(address).substringBefore(":").trim()

    private fun parsePort(address: String): Int? {
        val normalized = normalize(address)
        if (!normalized.contains(":")) return null
        return normalized.substringAfter(":").toIntOrNull()
    }

    private fun normalize(address: String): String = address
        .trim()
        .removePrefix("ipp://")
        .removePrefix("ipps://")
        .removePrefix("http://")
        .removePrefix("https://")
        .substringBefore("/")

    private const val TAG = "LEXPrint"
    private const val IPP_PORT = 631
    private const val RAW_PORT = 9100
}
