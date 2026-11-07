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
                title = "Devices",
                actions = {
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("Add", color = MaterialTheme.colorScheme.onPrimary)
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
                            "Find devices",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        discoveryStatus ?: "Discover compatible printers on your Wi-Fi network.",
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
                            onClick = if (discovering) onStopDiscovery else onStartDiscovery
                        ) {
                            Icon(
                                if (discovering) Icons.Filled.Refresh else Icons.Filled.Wifi,
                                contentDescription = null
                            )
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text(if (discovering) "Stop" else "Find devices")
                        }
                        OutlinedButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Manual")
                        }
                    }
                }
            }

            if (discovered.isNotEmpty()) {
                SectionHeader(title = "Discovered (${discovered.size})")
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
                            Button(onClick = { onAddDevice(device) }) { Text("Add") }
                        }
                    }
                }
            }

            SectionHeader(
                title = "Saved devices (${state.devices.size})",
                actionLabel = "Queue",
                onAction = onOpenQueue
            )
            if (state.devices.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Print,
                    title = "No saved devices",
                    message = "Find a printer on your network, add one manually, or try the demo device.",
                    actionLabel = "Add demo device",
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
        title = { Text("Add device") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("IP address or hostname") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownChoice(
                    label = "Type",
                    options = DeviceType.entries.map { it.label },
                    selected = typeText,
                    onSelected = { typeText = it }
                )
                SwitchRow(
                    title = "Color printing",
                    subtitle = "Supports color documents",
                    checked = color,
                    onCheckedChange = { color = it }
                )
                SwitchRow(
                    title = "Scanning",
                    subtitle = "Exposes a scanner",
                    checked = scan,
                    onCheckedChange = { scan = it }
                )
                SwitchRow(
                    title = "Web print",
                    subtitle = "Accepts web documents",
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
                            name = name.ifBlank { "Manual device" },
                            address = address,
                            type = type,
                            color = color,
                            scan = scan,
                            web = web
                        )
                    )
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
