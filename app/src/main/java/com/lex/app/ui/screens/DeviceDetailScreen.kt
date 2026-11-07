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
                title = device?.name ?: "Dispositivo",
                onBack = onBack,
                actions = {
                    if (device != null) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remover dispositivo")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (device == null) {
            Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                Text("Este dispositivo não está mais disponível.")
                TextButton(onClick = onBack) { Text("Voltar") }
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
                                "Dispositivo padrão",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    InfoRow("Modelo", device.model)
                    InfoRow("Tipo", device.type.label)
                    InfoRow("Endereço", device.address.ifBlank { "Não disponível" })
                    InfoRow("Cor", if (device.color) "Sim" else "Não")
                    InfoRow("Digitalização", if (device.scan) "Sim" else "Não")
                    InfoRow("Impressão web", if (device.web) "Sim" else "Não")
                }
            }

            SectionHeader(title = "Suprimentos")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TonerRow("Preto", device.blackToner, MaterialTheme.colorScheme.onSurface)
                    TonerRow("Ciano", device.cyanToner, Color(0xFF00838F))
                    TonerRow("Magenta", device.magentaToner, Color(0xFFAD1457))
                    TonerRow("Amarelo", device.yellowToner, Color(0xFFF9A825))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenPrint, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Print, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Imprimir")
                }
                Button(
                    onClick = onOpenScan,
                    modifier = Modifier.weight(1f),
                    enabled = device.supportsScan
                ) {
                    Icon(Icons.Filled.Scanner, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Digitalizar")
                }
            }

            if (!device.isDefault) {
                OutlinedButton(
                    onClick = { onSetDefault(device.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Definir como padrão")
                }
            }

            OutlinedButton(
                onClick = { showHelp = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Build, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Ajuda do dispositivo")
            }
        }
    }

    if (showDelete && device != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Remover dispositivo?") },
            text = { Text("${device.name} será removido do LEX. As configurações salvas permanecem neste dispositivo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelete = false
                        onRemove(device.id)
                        onBack()
                    }
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancelar") }
            }
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Ajuda do dispositivo") },
            text = {
                Column {
                    Text("• Verifique se o celular e a impressora estão na mesma rede.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Verifique se a impressora está ligada e pronta.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Se a descoberta falhar, adicione a impressora manualmente usando o endereço IP.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Reinicie o Wi-Fi se a impressora não aparecer.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Fechar") }
            }
        )
    }
}
