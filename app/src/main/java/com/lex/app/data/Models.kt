package com.lex.app.data

import java.util.UUID

enum class DeviceType(val label: String) {
    PRINTER("Printer"),
    SCANNER("Scanner"),
    MFP("Printer and Scanner")
}

enum class DeviceStatus(val label: String) {
    READY("Ready"),
    WARNING("Ready but check printer for warnings"),
    OFFLINE("Offline"),
    ERROR("Printer Error")
}

data class Device(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val model: String = "LEX compatible device",
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
    PRINT("Print"),
    SCAN("Scan"),
    COPY("Copy"),
    WEB("Web")
}

enum class JobStatus(val label: String) {
    QUEUED("Queued"),
    HELD("Held"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed")
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
    val orientation: String = "Portrait",
    val mediaSize: String = "A4",
    val allPages: Boolean = true,
    val pageRange: String = ""
)

data class ScanSettings(
    val source: String = "Camera",
    val colorMode: String = "Color",
    val dpi: Int = 300,
    val originalSize: String = "Auto Detect",
    val sides: String = "One-Sided"
)

data class WebHistory(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String = url,
    val visitedAt: Long = System.currentTimeMillis()
)

enum class SourceType(val label: String) {
    LOCAL("Local Files"),
    PHOTOS("Photos"),
    CAMERA("Camera"),
    WEB("Web"),
    CLOUD("Cloud")
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
    Source(type = SourceType.LOCAL, name = "Local Files", detail = "Documents and downloads"),
    Source(type = SourceType.PHOTOS, name = "Photos", detail = "Images on this device"),
    Source(type = SourceType.CAMERA, name = "Camera", detail = "Capture a new page"),
    Source(type = SourceType.WEB, name = "Web", detail = "Print a web page"),
    Source(type = SourceType.CLOUD, name = "Cloud", detail = "Connect a cloud account", enabled = false)
)

fun demoDevice(name: String = "LEX Demo Printer", address: String = "192.168.1.100"): Device = Device(
    name = name,
    model = "LEX MFP Demo",
    address = address,
    type = DeviceType.MFP,
    status = DeviceStatus.READY,
    isDefault = true
)
