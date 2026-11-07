package com.lex.app.data

import java.util.UUID

enum class DeviceType(val label: String) {
    PRINTER("Impressora"),
    SCANNER("Scanner"),
    MFP("Impressora e scanner")
}

enum class DeviceStatus(val label: String) {
    READY("Pronto"),
    WARNING("Pronto, mas verifique os avisos da impressora"),
    OFFLINE("Desconectado"),
    ERROR("Erro na impressora")
}

data class Device(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val model: String = "Dispositivo compatível com LEX",
    val address: String = "",
    val type: DeviceType = DeviceType.MFP,
    val status: DeviceStatus = DeviceStatus.READY,
    val isDefault: Boolean = false,
    val color: Boolean = true,
    val scan: Boolean = true,
    val web: Boolean = true,
    val blackToner: Int = 82,
    val cyanToner: Int = 68,
    val magentaToner: Int = 61,
    val yellowToner: Int = 74
) {
    val supportsPrint: Boolean get() = type != DeviceType.SCANNER
    val supportsScan: Boolean get() = scan && type != DeviceType.PRINTER
}

enum class JobKind(val label: String) {
    PRINT("Impressão"),
    SCAN("Digitalização"),
    COPY("Cópia"),
    WEB("Web")
}

enum class JobStatus(val label: String) {
    QUEUED("Na fila"),
    HELD("Retido"),
    PROCESSING("Processando"),
    COMPLETED("Concluído"),
    FAILED("Falhou")
}

data class PrintJob(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kind: JobKind,
    val deviceName: String,
    val status: JobStatus = JobStatus.QUEUED,
    val createdAt: Long = System.currentTimeMillis(),
    val pages: Int = 1,
    val color: Boolean = false,
    val duplex: Boolean = false,
    val detail: String = ""
)

data class PrintSettings(
    val copies: Int = 1,
    val color: Boolean = false,
    val duplex: Boolean = false,
    val orientation: String = "Retrato",
    val mediaSize: String = "A4",
    val allPages: Boolean = true,
    val pageRange: String = ""
)

data class ScanSettings(
    val source: String = "Câmera",
    val colorMode: String = "Colorido",
    val dpi: Int = 300,
    val originalSize: String = "Detecção automática",
    val sides: String = "Um lado"
)

data class WebHistory(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String = url,
    val visitedAt: Long = System.currentTimeMillis()
)

enum class SourceType(val label: String) {
    LOCAL("Arquivos locais"),
    PHOTOS("Fotos"),
    CAMERA("Câmera"),
    WEB("Web"),
    CLOUD("Nuvem")
}

data class Source(
    val id: String = UUID.randomUUID().toString(),
    val type: SourceType,
    val name: String,
    val detail: String = "",
    val enabled: Boolean = true
)

data class AppData(
    val onboardingDone: Boolean = false,
    val dataCollection: Boolean = false,
    val notifications: Boolean = true,
    val devices: List<Device> = emptyList(),
    val jobs: List<PrintJob> = emptyList(),
    val webHistory: List<WebHistory> = emptyList(),
    val printSettings: PrintSettings = PrintSettings(),
    val scanSettings: ScanSettings = ScanSettings(),
    val sources: List<Source> = defaultSources()
) {
    val defaultDevice: Device?
        get() = devices.firstOrNull { it.isDefault } ?: devices.firstOrNull()

    fun device(id: String?): Device? = devices.firstOrNull { it.id == id }
}

fun defaultSources(): List<Source> = listOf(
    Source(type = SourceType.LOCAL, name = "Arquivos locais", detail = "Documentos e arquivos baixados"),
    Source(type = SourceType.PHOTOS, name = "Fotos", detail = "Imagens neste dispositivo"),
    Source(type = SourceType.CAMERA, name = "Câmera", detail = "Capturar uma nova página"),
    Source(type = SourceType.WEB, name = "Web", detail = "Imprimir uma página da web"),
    Source(type = SourceType.CLOUD, name = "Nuvem", detail = "Conectar uma conta na nuvem", enabled = false)
)

fun demoDevice(name: String = "Impressora de Demonstração LEX", address: String = "192.168.1.100"): Device = Device(
    name = name,
    model = "Multifuncional de Demonstração LEX",
    address = address,
    type = DeviceType.MFP,
    status = DeviceStatus.READY,
    isDefault = true
)
