package com.lex.app.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.lex.app.data.Device
import com.lex.app.data.DeviceType

class DiscoveryManager(
    context: Context,
    private val onFound: (Device) -> Unit,
    private val onStatus: (String) -> Unit
) {

    private val nsdManager = context.applicationContext
        .getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val listeners = mutableListOf<NsdManager.DiscoveryListener>()
    private val serviceTypes = listOf(
        "_ipp._tcp.",
        "_ipps._tcp.",
        "_printer._tcp.",
        "_pdl-datastream._tcp.",
        "_scanner._tcp."
    )

    private var running = false

    fun start() {
        if (running) return
        val manager = nsdManager
        if (manager == null) {
            onStatus("A busca de rede não está disponível neste dispositivo.")
            return
        }
        running = true
        serviceTypes.forEach { type ->
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) = Unit

                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    if (!serviceInfo.serviceType.contains(type.removeSuffix("."))) return
                    resolve(manager, serviceInfo)
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit

                override fun onDiscoveryStopped(serviceType: String) = Unit

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    onStatus("Não foi possível iniciar a busca por $serviceType (código $errorCode).")
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
            }
            listeners += listener
            runCatching {
                manager.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener)
            }.onFailure {
                onStatus("Erro na busca de rede: ${it.message ?: "desconhecido"}")
            }
        }
        onStatus("Procurando impressoras na sua rede…")
    }

    @Suppress("DEPRECATION")
    private fun resolve(manager: NsdManager, info: NsdServiceInfo) {
        runCatching {
            manager.resolveService(info, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "resolve failed for ${serviceInfo.serviceName}: $errorCode")
                }

                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    val address = serviceInfo.host?.hostAddress ?: ""
                    val scanner = serviceInfo.serviceType.contains("scanner", ignoreCase = true)
                    val device = Device(
                        name = serviceInfo.serviceName.ifBlank { "Dispositivo de rede" },
                        model = if (scanner) "Scanner de rede" else "Impressora de rede",
                        address = address,
                        type = if (scanner) DeviceType.SCANNER else DeviceType.MFP,
                        color = !scanner,
                        scan = scanner,
                        web = false
                    )
                    onFound(device)
                }
            })
        }.onFailure {
            onStatus("Não foi possível resolver um dispositivo encontrado.")
        }
    }

    fun stop() {
        if (!running) return
        val manager = nsdManager
        listeners.forEach { listener ->
            runCatching { manager?.stopServiceDiscovery(listener) }
        }
        listeners.clear()
        running = false
    }

    private companion object {
        const val TAG = "LexDiscovery"
    }
}
