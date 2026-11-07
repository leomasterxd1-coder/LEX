package com.lex.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var showHelp by remember { mutableStateOf(false) }
    var showCloud by remember { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LexTopBar(
                title = "Localizar dispositivo",
                actions = {
                    IconButton(
                        onClick = { if (discovering) onStopDiscovery() else onStartDiscovery() }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Buscar novamente")
                    }
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Filled.HelpOutline, contentDescription = "Ajuda")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (discovering) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Buscando rede...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                Spacer(Modifier.height(180.dp))
            } else if (discovered.isNotEmpty()) {
                DevicesSectionLabel("ENCONTRADOS")
                discovered.forEach { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(device.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                listOf(device.model, device.address)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = { onAddDevice(device) }) { Text("Adicionar") }
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(32.dp))
                Text(
                    discoveryStatus ?: "Nenhum dispositivo encontrado.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(180.dp))
            }

            DevicesSectionLabel("OUTRO")
            DeviceListRow(Icons.Filled.Cloud, "Nuvem de gerenciamento de impressão") {
                showCloud = true
            }
            DeviceListRow(Icons.Filled.SettingsEthernet, "Inserir endereço de rede") {
                showAddDialog = true
            }
            DeviceListRow(Icons.Filled.QrCode2, "Código de QR") {
                showQr = true
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

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Ajuda") },
            text = {
                Column {
                    Text("• Verifique se o celular e a impressora estão na mesma rede.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Verifique se a impressora está ligada e pronta.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Se a busca falhar, use “Inserir endereço de rede”.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Fechar") }
            }
        )
    }

    if (showCloud) {
        AlertDialog(
            onDismissRequest = { showCloud = false },
            title = { Text("Nuvem de gerenciamento de impressão") },
            text = { Text("Este recurso é uma demonstração nesta versão independente do LEX.") },
            confirmButton = {
                TextButton(onClick = { showCloud = false }) { Text("Fechar") }
            }
        )
    }

    if (showQr) {
        AlertDialog(
            onDismissRequest = { showQr = false },
            title = { Text("Código de QR") },
            text = { Text("A leitura de QR estará disponível em breve nesta versão independente.") },
            confirmButton = {
                TextButton(onClick = { showQr = false }) { Text("Fechar") }
            }
        )
    }
}

@Composable
private fun DevicesSectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DeviceListRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(20.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 62.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    )
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
                    label = { Text("Endereço IP da impressora") },
                    placeholder = { Text("ex.: 192.168.1.50 ou 192.168.1.50:9100") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "A impressora precisa estar ligada e na mesma rede do celular.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
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
                            address = address.trim(),
                            type = type,
                            color = color,
                            scan = scan,
                            web = web
                        )
                    )
                },
                enabled = address.isNotBlank()
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
