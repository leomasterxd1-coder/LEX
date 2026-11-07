package com.lex.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.Device
import com.lex.app.data.DeviceStatus
import com.lex.app.platform.PrintController
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.theme.LexAmber
import com.lex.app.ui.theme.LexGreen
import com.lex.app.ui.theme.LexRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current
    val device = state.device(deviceId)
    var showDelete by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            LexTopBar(
                title = "",
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
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Print,
                    contentDescription = null,
                    tint = Color(0xFF4A4A4A),
                    modifier = Modifier.size(108.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        device.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        device.model,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        device.address.ifBlank { "Endereço não disponível" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Preto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { device.blackToner.coerceIn(0, 100) / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp),
                            color = Color(0xFF333333),
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = {
                        if (testing) return@Button
                        testing = true
                        val stamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                            .format(Date())
                        scope.launch {
                            PrintController.printText(
                                context,
                                "LEX — Página de teste\n\nDispositivo: ${device.name}\nModelo: ${device.model}\nEndereço: ${device.address}\nData: $stamp",
                                "Página de teste",
                                state.printSettings
                            ).onSuccess {
                                Toast.makeText(context, "Abrindo a página de teste…", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    it.message ?: "Não foi possível gerar a página de teste.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            testing = false
                        }
                    },
                    enabled = !testing
                ) {
                    if (testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("PÁGINA DE TESTE")
                    }
                }
            }

            DetailSectionLabel("STATUS")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(statusColor(device.status), CircleShape)
                )
                Spacer(Modifier.width(20.dp))
                Text(
                    device.status.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            DetailSectionLabel("DETALHES")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        device.model,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        device.type.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
}

@Composable
private fun DetailSectionLabel(text: String) {
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

private fun statusColor(status: DeviceStatus): Color = when (status) {
    DeviceStatus.READY -> LexGreen
    DeviceStatus.WARNING -> LexAmber
    DeviceStatus.OFFLINE -> Color.Gray
    DeviceStatus.ERROR -> LexRed
}
