package com.lex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.Device
import com.lex.app.data.DeviceType
import com.lex.app.ui.components.DeviceCard
import com.lex.app.ui.components.DropdownChoice
import com.lex.app.ui.components.EmptyState
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader
import com.lex.app.ui.components.SwitchRow

@Composable
fun DevicesScreen(
    state: AppData,
    discovered: List<Device>,
    discovering: Boolean,
    discoveryStatus: String?,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onAddDevice: (Device) -> Unit,
    onSetDefault: (String) -> Unit,
    onOpenDevice: (String) -> Unit,
    onOpenQueue: () -> Unit,
    onAddDemoDevice: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LexTopBar(
                title = "Dispositivos",
                actions = {
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("Adicionar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Text(
                            "Buscar dispositivos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        discoveryStatus ?: "Descubra impressoras compatíveis na sua rede Wi-Fi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (discovering) {
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = if (discovering) onStopDiscovery else onStartDiscovery,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                if (discovering) Icons.Filled.Refresh else Icons.Filled.Wifi,
                                contentDescription = null
                            )
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text(if (discovering) "Parar" else "Buscar dispositivos")
                        }
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Manual")
                        }
                    }
                }
            }

            if (discovered.isNotEmpty()) {
                SectionHeader(title = "Encontrados (${discovered.size})")
                discovered.forEach { device ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    listOf(device.model, device.address)
                                        .filter { it.isNotBlank() }.joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(onClick = { onAddDevice(device) }) { Text("Adicionar") }
                        }
                    }
                }
            }

            SectionHeader(
                title = "Dispositivos salvos (${state.devices.size})",
                actionLabel = "Fila",
                onAction = onOpenQueue
            )
            if (state.devices.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Print,
                    title = "Nenhum dispositivo salvo",
                    message = "Encontre uma impressora na rede, adicione manualmente ou use o dispositivo de demonstração.",
                    actionLabel = "Adicionar dispositivo de demonstração",
                    onAction = onAddDemoDevice
                )
            } else {
                state.devices.forEach { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onOpenDevice(device.id) },
                        onSetDefault = { onSetDefault(device.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { device ->
                onAddDevice(device)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAdd: (Device) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var typeText by remember { mutableStateOf(DeviceType.MFP.label) }
    var color by remember { mutableStateOf(true) }
    var scan by remember { mutableStateOf(true) }
    var web by remember { mutableStateOf(true) }

    val type = when (typeText) {
        DeviceType.PRINTER.label -> DeviceType.PRINTER
        DeviceType.SCANNER.label -> DeviceType.SCANNER
        else -> DeviceType.MFP
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar dispositivo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereço IP ou nome do host") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownChoice(
                    label = "Tipo",
                    options = DeviceType.entries.map { it.label },
                    selected = typeText,
                    onSelected = { typeText = it }
                )
                SwitchRow(
                    title = "Impressão colorida",
                    subtitle = "Suporta documentos coloridos",
                    checked = color,
                    onCheckedChange = { color = it }
                )
                SwitchRow(
                    title = "Digitalização",
                    subtitle = "Expõe um scanner",
                    checked = scan,
                    onCheckedChange = { scan = it }
                )
                SwitchRow(
                    title = "Impressão web",
                    subtitle = "Aceita documentos da web",
                    checked = web,
                    onCheckedChange = { web = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(
                        Device(
                            name = name.ifBlank { "Dispositivo manual" },
                            address = address,
                            type = type,
                            color = color,
                            scan = scan,
                            web = web
                        )
                    )
                }
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
