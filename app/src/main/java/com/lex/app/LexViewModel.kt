package com.lex.app

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lex.app.data.AppData
import com.lex.app.data.AppRepository
import com.lex.app.data.Device
import com.lex.app.data.DeviceStatus
import com.lex.app.data.DeviceType
import com.lex.app.data.JobKind
import com.lex.app.data.JobStatus
import com.lex.app.data.PrintJob
import com.lex.app.data.PrintSettings
import com.lex.app.data.ScanSettings
import com.lex.app.data.WebHistory
import com.lex.app.data.demoDevice
import com.lex.app.service.DiscoveryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class LexViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    val data: StateFlow<AppData> = repository.data

    private val _discovered = MutableStateFlow<List<Device>>(emptyList())
    val discovered: StateFlow<List<Device>> = _discovered.asStateFlow()

    private val _discovering = MutableStateFlow(false)
    val discovering: StateFlow<Boolean> = _discovering.asStateFlow()

    private val _discoveryStatus = MutableStateFlow<String?>(null)
    val discoveryStatus: StateFlow<String?> = _discoveryStatus.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var discoveryManager: DiscoveryManager? = null

    fun completeOnboarding() = repository.completeOnboarding()

    fun setDataCollection(enabled: Boolean) = repository.setDataCollection(enabled)

    fun setNotifications(enabled: Boolean) = repository.setNotifications(enabled)

    fun setPrintSettings(settings: PrintSettings) = repository.setPrintSettings(settings)

    fun setScanSettings(settings: ScanSettings) = repository.setScanSettings(settings)

    fun addDevice(device: Device) {
        repository.addDevice(device)
        _events.tryEmit("${device.name} added.")
    }

    fun addDemoDevice() {
        addDevice(demoDevice())
    }

    fun removeDevice(id: String) {
        repository.removeDevice(id)
        _events.tryEmit("Device removed.")
    }

    fun setDefaultDevice(id: String) {
        repository.setDefaultDevice(id)
        _events.tryEmit("Default device updated.")
    }

    fun startDiscovery() {
        if (_discovering.value) return
        _discovered.value = emptyList()
        _discovering.value = true
        _discoveryStatus.value = "Searching for printers on your network…"
        discoveryManager = DiscoveryManager(
            context = getApplication(),
            onFound = { device ->
                if (_discovered.value.none {
                        it.name == device.name && it.address == device.address
                    }
                ) {
                    _discovered.value = _discovered.value + device
                }
            },
            onStatus = { message -> _discoveryStatus.value = message }
        ).also { it.start() }
        viewModelScope.launch {
            delay(20_000)
            if (_discovering.value) stopDiscovery()
        }
    }

    fun stopDiscovery() {
        discoveryManager?.stop()
        discoveryManager = null
        _discovering.value = false
        if (_discovered.value.isEmpty()) {
            _discoveryStatus.value = "No devices found. Add one manually or try again."
        } else {
            _discoveryStatus.value = "Discovery finished."
        }
    }

    fun createJob(
        name: String,
        kind: JobKind,
        device: Device?,
        pages: Int,
        color: Boolean,
        duplex: Boolean,
        detail: String = "",
        notify: Boolean = true
    ) {
        val job = PrintJob(
            name = name,
            kind = kind,
            deviceName = device?.name ?: "No device selected",
            pages = pages,
            color = color,
            duplex = duplex,
            detail = detail
        )
        repository.addJob(job)
        viewModelScope.launch {
            delay(2_000)
            repository.updateJob(job.id) { it.copy(status = JobStatus.PROCESSING) }
            delay(4_000)
            repository.updateJob(job.id) { it.copy(status = JobStatus.COMPLETED) }
            _events.emit("${kind.label} job completed: $name")
            if (notify) notifyJobCompleted(job)
        }
    }

    fun releaseJob(id: String) {
        repository.updateJob(id) { it.copy(status = JobStatus.PROCESSING) }
        viewModelScope.launch {
            delay(2_000)
            repository.updateJob(id) { it.copy(status = JobStatus.COMPLETED) }
            _events.emit("Job released and completed.")
        }
    }

    fun releaseAll() {
        val pending = repository.data.value.jobs.filter {
            it.status == JobStatus.QUEUED || it.status == JobStatus.HELD
        }
        pending.forEach { releaseJob(it.id) }
        if (pending.isNotEmpty()) _events.tryEmit("Releasing ${pending.size} job(s).")
    }

    fun deleteJob(id: String) {
        repository.deleteJob(id)
        _events.tryEmit("Job removed.")
    }

    fun clearFinishedJobs() {
        repository.clearFinishedJobs()
        _events.tryEmit("Finished jobs cleared.")
    }

    fun addWebHistory(url: String, title: String) {
        if (url.isBlank()) return
        repository.addWebHistory(WebHistory(url = url, title = title.ifBlank { url }))
    }

    fun clearWebHistory() {
        repository.clearWebHistory()
        _events.tryEmit("Web history cleared.")
    }

    fun toggleSource(id: String, enabled: Boolean) = repository.updateSource(id, enabled)

    fun resetApp() {
        repository.reset()
        _events.tryEmit("LEX was reset.")
    }

    fun importDevices(raw: String): Result<Int> = runCatching {
        val text = raw.trim()
        require(text.isNotEmpty()) { "Paste a JSON list of devices." }
        val array = if (text.startsWith("[")) JSONArray(text) else JSONArray().put(JSONObject(text))
        val imported = buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.optString("name").ifBlank { "Imported device" }
                val address = item.optString("address")
                add(
                    Device(
                        name = name,
                        model = item.optString("model", "LEX compatible device"),
                        address = address,
                        type = runCatching {
                            DeviceType.valueOf(item.optString("type", DeviceType.MFP.name))
                        }.getOrDefault(DeviceType.MFP),
                        status = DeviceStatus.READY
                    )
                )
            }
        }
        require(imported.isNotEmpty()) { "No devices were found in the JSON." }
        repository.addDevices(imported)
        imported.size
    }

    fun devicesAsJson(): String {
        val array = JSONArray()
        repository.data.value.devices.forEach { device ->
            array.put(
                JSONObject().apply {
                    put("name", device.name)
                    put("model", device.model)
                    put("address", device.address)
                    put("type", device.type.name)
                }
            )
        }
        return array.toString(2)
    }

    private fun notifyJobCompleted(job: PrintJob) {
        val context = getApplication<Application>()
        if (!repository.data.value.notifications) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_jobs),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notif_channel_jobs_desc)
                }
            )
        }
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_lex)
            .setContentTitle("${job.kind.label} job completed")
            .setContentText(job.name)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        runCatching { manager.notify(job.id.hashCode(), notification) }
    }

    override fun onCleared() {
        discoveryManager?.stop()
        super.onCleared()
    }

    private companion object {
        const val CHANNEL_ID = "lex_jobs"
    }
}
