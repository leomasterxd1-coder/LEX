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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.ui.components.InfoRow
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader
import com.lex.app.ui.components.StatusBadge
import com.lex.app.ui.components.TonerRow

@Composable
fun DeviceDetailScreen(
    state: AppData,
    deviceId: String,
    onBack: () -> Unit,
    onSetDefault: (String) -> Unit,
    onRemove: (String) -> Unit,
    onOpenPrint: () -> Unit,
    onOpenScan: () -> Unit
) {
    val device = state.device(deviceId)
    var showDelete by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LexTopBar(
                title = device?.name ?: "Device",
                onBack = onBack,
                actions = {
                    if (device != null) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove device")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (device == null) {
            Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                Text("This device is no longer available.")
                TextButton(onClick = onBack) { Text("Go back") }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(device.status)
                        if (device.isDefault) {
                            Text(
                                "Default device",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    InfoRow("Model", device.model)
                    InfoRow("Type", device.type.label)
                    InfoRow("Address", device.address.ifBlank { "Not available" })
                    InfoRow("Color", if (device.color) "Yes" else "No")
                    InfoRow("Scanning", if (device.scan) "Yes" else "No")
                    InfoRow("Web print", if (device.web) "Yes" else "No")
                }
            }

            SectionHeader(title = "Supplies")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TonerRow("Black", device.blackToner, MaterialTheme.colorScheme.onSurface)
                    TonerRow("Cyan", device.cyanToner, Color(0xFF00838F))
                    TonerRow("Magenta", device.magentaToner, Color(0xFFAD1457))
                    TonerRow("Yellow", device.yellowToner, Color(0xFFF9A825))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenPrint, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Print, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Print")
                }
                Button(
                    onClick = onOpenScan,
                    modifier = Modifier.weight(1f),
                    enabled = device.supportsScan
                ) {
                    Icon(Icons.Filled.Scanner, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Scan")
                }
            }

            if (!device.isDefault) {
                OutlinedButton(
                    onClick = { onSetDefault(device.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Set as default")
                }
            }

            OutlinedButton(
                onClick = { showHelp = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Build, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Device help")
            }
        }
    }

    if (showDelete && device != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Remove device?") },
            text = { Text("${device.name} will be removed from LEX. Saved settings stay on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelete = false
                        onRemove(device.id)
                        onBack()
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Device help") },
            text = {
                Column {
                    Text("• Make sure the mobile device and the printer are on the same network.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Make sure the printer is awake and in a ready state.")
                    Spacer(Modifier.height(8.dp))
                    Text("• If discovery fails, add the printer manually using its IP address.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Restart Wi-Fi if the printer does not appear.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Close") }
            }
        )
    }
}
