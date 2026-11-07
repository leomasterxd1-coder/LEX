package com.lex.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AppRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("lex_state", Context.MODE_PRIVATE)

    private val _data = MutableStateFlow(read())
    val data: StateFlow<AppData> = _data.asStateFlow()

    private fun read(): AppData {
        val raw = prefs.getString(KEY_STATE, null) ?: return AppData()
        return runCatching { fromJson(JSONObject(raw)) }.getOrDefault(AppData())
    }

    private fun write(value: AppData) {
        prefs.edit().putString(KEY_STATE, toJson(value).toString()).apply()
        _data.value = value
    }

    private fun update(transform: (AppData) -> AppData) = write(transform(_data.value))

    fun completeOnboarding() = update { it.copy(onboardingDone = true) }

    fun setDataCollection(enabled: Boolean) = update { it.copy(dataCollection = enabled) }

    fun setNotifications(enabled: Boolean) = update { it.copy(notifications = enabled) }

    fun addDevice(device: Device) = update { current ->
        val withoutDuplicate = current.devices.filterNot {
            it.address.isNotBlank() && it.address.equals(device.address, ignoreCase = true)
        }
        val makeDefault = withoutDuplicate.isEmpty() || device.isDefault
        val next = withoutDuplicate.map { if (makeDefault) it.copy(isDefault = false) else it } + device.copy(isDefault = makeDefault)
        current.copy(devices = next)
    }

    fun addDevices(devices: List<Device>) = devices.forEach(::addDevice)

    fun removeDevice(id: String) = update { current ->
        val remaining = current.devices.filterNot { it.id == id }
        val fixed = if (remaining.none { it.isDefault } && remaining.isNotEmpty()) {
            remaining.mapIndexed { index, device -> if (index == 0) device.copy(isDefault = true) else device }
        } else remaining
        current.copy(devices = fixed)
    }

    fun updateDevice(device: Device) = update { current ->
        current.copy(devices = current.devices.map { if (it.id == device.id) device else it })
    }

    fun setDefaultDevice(id: String) = update { current ->
        current.copy(devices = current.devices.map { it.copy(isDefault = it.id == id) })
    }

    fun addJob(job: PrintJob) = update { it.copy(jobs = listOf(job) + it.jobs.take(99)) }

    fun updateJob(id: String, transform: (PrintJob) -> PrintJob) = update { current ->
        current.copy(jobs = current.jobs.map { if (it.id == id) transform(it) else it })
    }

    fun deleteJob(id: String) = update { current ->
        current.copy(jobs = current.jobs.filterNot { it.id == id })
    }

    fun clearFinishedJobs() = update { current ->
        current.copy(jobs = current.jobs.filterNot {
            it.status == JobStatus.COMPLETED || it.status == JobStatus.FAILED
        })
    }

    fun setPrintSettings(settings: PrintSettings) = update { it.copy(printSettings = settings) }

    fun setScanSettings(settings: ScanSettings) = update { it.copy(scanSettings = settings) }

    fun addWebHistory(entry: WebHistory) = update { current ->
        val next = (listOf(entry) + current.webHistory.filterNot { it.url == entry.url }).take(50)
        current.copy(webHistory = next)
    }

    fun clearWebHistory() = update { it.copy(webHistory = emptyList()) }

    fun updateSource(id: String, enabled: Boolean) = update { current ->
        current.copy(sources = current.sources.map { if (it.id == id) it.copy(enabled = enabled) else it })
    }

    fun reset() {
        prefs.edit().clear().apply()
        _data.value = AppData()
    }

    private companion object {
        const val KEY_STATE = "state"

        fun toJson(data: AppData): JSONObject = JSONObject().apply {
            put("onboardingDone", data.onboardingDone)
            put("dataCollection", data.dataCollection)
            put("notifications", data.notifications)
            put("devices", JSONArray().also { array -> data.devices.forEach { array.put(deviceToJson(it)) } })
            put("jobs", JSONArray().also { array -> data.jobs.forEach { array.put(jobToJson(it)) } })
            put("webHistory", JSONArray().also { array -> data.webHistory.forEach { array.put(historyToJson(it)) } })
            put("printSettings", printSettingsToJson(data.printSettings))
            put("scanSettings", scanSettingsToJson(data.scanSettings))
            put("sources", JSONArray().also { array -> data.sources.forEach { array.put(sourceToJson(it)) } })
        }

        fun fromJson(json: JSONObject): AppData = AppData(
            onboardingDone = json.optBoolean("onboardingDone", false),
            dataCollection = json.optBoolean("dataCollection", false),
            notifications = json.optBoolean("notifications", true),
            devices = json.optJSONArray("devices").mapObjects(::deviceFromJson),
            jobs = json.optJSONArray("jobs").mapObjects(::jobFromJson),
            webHistory = json.optJSONArray("webHistory").mapObjects(::historyFromJson),
            printSettings = json.optJSONObject("printSettings")?.let(::printSettingsFromJson) ?: PrintSettings(),
            scanSettings = json.optJSONObject("scanSettings")?.let(::scanSettingsFromJson) ?: ScanSettings(),
            sources = normalizeSources(json.optJSONArray("sources").mapObjects(::sourceFromJson))
        )

        fun deviceToJson(device: Device) = JSONObject().apply {
            put("id", device.id)
            put("name", device.name)
            put("model", device.model)
            put("address", device.address)
            put("type", device.type.name)
            put("status", device.status.name)
            put("isDefault", device.isDefault)
            put("color", device.color)
            put("scan", device.scan)
            put("web", device.web)
            put("blackToner", device.blackToner)
            put("cyanToner", device.cyanToner)
            put("magentaToner", device.magentaToner)
            put("yellowToner", device.yellowToner)
        }

        fun deviceFromJson(json: JSONObject) = Device(
            id = json.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
            name = json.optString("name", "Impressora"),
            model = json.optString("model", "Dispositivo compatível com LEX"),
            address = json.optString("address"),
            type = enumOrDefault(json.optString("type"), DeviceType.MFP),
            status = enumOrDefault(json.optString("status"), DeviceStatus.READY),
            isDefault = json.optBoolean("isDefault", false),
            color = json.optBoolean("color", true),
            scan = json.optBoolean("scan", true),
            web = json.optBoolean("web", true),
            blackToner = json.optInt("blackToner", 82),
            cyanToner = json.optInt("cyanToner", 68),
            magentaToner = json.optInt("magentaToner", 61),
            yellowToner = json.optInt("yellowToner", 74)
        )

        fun jobToJson(job: PrintJob) = JSONObject().apply {
            put("id", job.id)
            put("name", job.name)
            put("kind", job.kind.name)
            put("deviceName", job.deviceName)
            put("status", job.status.name)
            put("createdAt", job.createdAt)
            put("pages", job.pages)
            put("color", job.color)
            put("duplex", job.duplex)
            put("detail", job.detail)
        }

        fun jobFromJson(json: JSONObject) = PrintJob(
            id = json.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
            name = json.optString("name", "Trabalho"),
            kind = enumOrDefault(json.optString("kind"), JobKind.PRINT),
            deviceName = json.optString("deviceName", "Impressora"),
            status = enumOrDefault(json.optString("status"), JobStatus.QUEUED),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            pages = json.optInt("pages", 1),
            color = json.optBoolean("color", false),
            duplex = json.optBoolean("duplex", false),
            detail = json.optString("detail")
        )

        fun historyToJson(item: WebHistory) = JSONObject().apply {
            put("id", item.id)
            put("url", item.url)
            put("title", item.title)
            put("visitedAt", item.visitedAt)
        }

        fun historyFromJson(json: JSONObject) = WebHistory(
            id = json.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
            url = json.optString("url"),
            title = json.optString("title"),
            visitedAt = json.optLong("visitedAt", System.currentTimeMillis())
        )

        fun printSettingsToJson(settings: PrintSettings) = JSONObject().apply {
            put("copies", settings.copies)
            put("color", settings.color)
            put("duplex", settings.duplex)
            put("orientation", settings.orientation)
            put("mediaSize", settings.mediaSize)
            put("allPages", settings.allPages)
            put("pageRange", settings.pageRange)
        }

        fun printSettingsFromJson(json: JSONObject) = PrintSettings(
            copies = json.optInt("copies", 1).coerceIn(1, 999),
            color = json.optBoolean("color", false),
            duplex = json.optBoolean("duplex", false),
            orientation = ptOrientation(json.optString("orientation", "Retrato")),
            mediaSize = ptMedia(json.optString("mediaSize", "A4")),
            allPages = json.optBoolean("allPages", true),
            pageRange = json.optString("pageRange")
        )

        fun scanSettingsToJson(settings: ScanSettings) = JSONObject().apply {
            put("source", settings.source)
            put("colorMode", settings.colorMode)
            put("dpi", settings.dpi)
            put("originalSize", settings.originalSize)
            put("sides", settings.sides)
        }

        fun scanSettingsFromJson(json: JSONObject) = ScanSettings(
            source = ptSource(json.optString("source", "Câmera")),
            colorMode = ptColorMode(json.optString("colorMode", "Colorido")),
            dpi = json.optInt("dpi", 300),
            originalSize = ptOriginalSize(json.optString("originalSize", "Detecção automática")),
            sides = ptSides(json.optString("sides", "Um lado"))
        )

        fun sourceToJson(source: Source) = JSONObject().apply {
            put("id", source.id)
            put("type", source.type.name)
            put("name", source.name)
            put("detail", source.detail)
            put("enabled", source.enabled)
        }

        fun sourceFromJson(json: JSONObject) = Source(
            id = json.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
            type = enumOrDefault(json.optString("type"), SourceType.LOCAL),
            name = json.optString("name", "Fonte"),
            detail = json.optString("detail"),
            enabled = json.optBoolean("enabled", true)
        )

        fun normalizeSources(loaded: List<Source>): List<Source> {
            if (loaded.isEmpty()) return defaultSources()
            return defaultSources().map { def ->
                loaded.firstOrNull { it.type == def.type }?.copy(name = def.name, detail = def.detail) ?: def
            }
        }

        fun ptOrientation(raw: String): String = when (raw) {
            "Portrait", "Retrato" -> "Retrato"
            "Landscape", "Paisagem" -> "Paisagem"
            else -> raw
        }

        fun ptMedia(raw: String): String = when (raw) {
            "Letter", "Carta" -> "Carta"
            "Legal", "Ofício", "Oficio" -> "Ofício"
            else -> raw
        }

        fun ptColorMode(raw: String): String = when (raw) {
            "Color", "Colorido" -> "Colorido"
            "Black and White", "Preto e branco" -> "Preto e branco"
            "Grayscale", "Escala de cinza" -> "Escala de cinza"
            else -> raw
        }

        fun ptSource(raw: String): String = when (raw) {
            "Camera", "Câmera" -> "Câmera"
            else -> raw
        }

        fun ptOriginalSize(raw: String): String = when (raw) {
            "Auto Detect", "Detecção automática" -> "Detecção automática"
            else -> raw
        }

        fun ptSides(raw: String): String = when (raw) {
            "One-Sided", "Um lado" -> "Um lado"
            "Two-Sided (Long Edge)", "Frente e verso (borda longa)" -> "Frente e verso (borda longa)"
            "Two-Sided (Short Edge)", "Frente e verso (borda curta)" -> "Frente e verso (borda curta)"
            else -> raw
        }

        inline fun <reified T : Enum<T>> enumOrDefault(raw: String, fallback: T): T =
            runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)

        inline fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T): List<T> {
            if (this == null) return emptyList()
            return buildList {
                for (index in 0 until length()) {
                    optJSONObject(index)?.let { add(transform(it)) }
                }
            }
        }
    }
}
