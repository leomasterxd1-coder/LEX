package com.lex.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.JobKind
import com.lex.app.data.PrintSettings
import com.lex.app.platform.PdfUtils
import com.lex.app.platform.PrintController
import com.lex.app.ui.components.DropdownChoice
import com.lex.app.ui.components.InfoRow
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader
import com.lex.app.ui.components.SwitchRow
import kotlinx.coroutines.launch

@Composable
fun PrintScreen(
    state: AppData,
    onCreateJob: (String, JobKind, Int, Boolean, Boolean, String) -> Unit,
    onPrintSettingsChange: (PrintSettings) -> Unit,
    onSelectDevice: (String) -> Unit,
    onBack: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenWeb: () -> Unit,
    onOpenScan: () -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("") }
    var selectedMeta by remember { mutableStateOf("") }
    var selectedDeviceId by remember { mutableStateOf(state.defaultDevice?.id) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var printing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.defaultDevice?.id) {
        if (selectedDeviceId == null) selectedDeviceId = state.defaultDevice?.id
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            selectedUri = uri
            selectedName = PdfUtils.displayName(context, uri)
            selectedMeta = listOf(
                context.contentResolver.getType(uri) ?: "documento",
                PdfUtils.sizeLabel(context, uri)
            ).filter { it.isNotBlank() }.joinToString(" · ")
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            selectedName = PdfUtils.displayName(context, uri)
            selectedMeta = listOf(
                context.contentResolver.getType(uri) ?: "imagem",
                PdfUtils.sizeLabel(context, uri)
            ).filter { it.isNotBlank() }.joinToString(" · ")
        }
    }

    val selectedDevice = state.device(selectedDeviceId) ?: state.defaultDevice
    val printSettings = state.printSettings

    fun performPrint() {
        val uri = selectedUri
        if (uri == null) {
            Toast.makeText(context, "Escolha um arquivo ou foto primeiro.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDevice == null) {
            Toast.makeText(context, "Adicione ou selecione um dispositivo primeiro.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!selectedDevice.supportsPrint) {
            Toast.makeText(context, "Este dispositivo não suporta impressão.", Toast.LENGTH_SHORT).show()
            return
        }
        if (printing) return
        printing = true
        scope.launch {
            PrintController.printUri(
                context,
                uri,
                selectedName.ifBlank { "Documento" },
                printSettings
            )
                .onSuccess {
                    onCreateJob(
                        selectedName.ifBlank { "Documento" },
                        JobKind.PRINT,
                        printSettings.copies,
                        printSettings.color,
                        printSettings.duplex,
                        "Enviado para ${selectedDevice.name}"
                    )
                    Toast.makeText(context, "Abrindo a janela de impressão do Android…", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(
                        context,
                        it.message ?: "Não foi possível iniciar a impressão.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            printing = false
        }
    }

    Scaffold(
        topBar = {
            LexTopBar(title = "Imprimir", onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(title = "Impressora")
            if (state.devices.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Nenhum dispositivo ainda",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Descubra uma impressora ou adicione o dispositivo de demonstração.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Button(
                            onClick = onOpenDevices,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Adicionar")
                        }
                    }
                }
            } else {
                DropdownChoice(
                    label = "Dispositivo selecionado",
                    options = state.devices.map { it.name },
                    selected = selectedDevice?.name ?: state.devices.first().name,
                    onSelected = { name ->
                        val id = state.devices.firstOrNull { it.name == name }?.id
                        selectedDeviceId = id
                        if (id != null) onSelectDevice(id)
                    }
                )
                if (selectedDevice != null) {
                    Text(
                        listOf(selectedDevice.model, selectedDevice.address)
                            .filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SectionHeader(title = "Documento")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                selectedName.ifBlank { "Nenhum arquivo selecionado" },
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                selectedMeta.ifBlank {
                                    "PDF, imagens e arquivos de texto são suportados."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                filePicker.launch(arrayOf("application/pdf", "image/*", "text/*"))
                            }
                        ) { Text("Escolher arquivo") }
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Foto")
                        }
                    }
                }
            }

            SectionHeader(
                title = "Configurações de impressão",
                actionLabel = "Alterar",
                onAction = { showSettingsDialog = true }
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Cópias", printSettings.copies.toString())
                    InfoRow("Cor", if (printSettings.color) "Colorido" else "Preto e branco")
                    InfoRow("Lados", if (printSettings.duplex) "Frente e verso" else "Um lado")
                    InfoRow("Tamanho do papel", printSettings.mediaSize)
                    InfoRow("Orientação", printSettings.orientation)
                    InfoRow(
                        "Páginas",
                        if (printSettings.allPages) "Todas as páginas" else printSettings.pageRange.ifBlank { "Personalizado" }
                    )
                }
            }

            Button(
                onClick = { performPrint() },
                enabled = !printing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (printing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Print, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Text("Imprimir")
                }
            }

            SectionHeader(title = "Mais formas de imprimir")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onOpenWeb, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Language, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Página da web")
                }
                OutlinedButton(onClick = onOpenScan, modifier = Modifier.weight(1f)) {
                    Text("Digitalizar primeiro")
                }
            }
        }
    }

    if (showSettingsDialog) {
        var draft by remember(printSettings) { mutableStateOf(printSettings) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Configurações de impressão", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    StepperRow(
                        label = "Cópias",
                        value = draft.copies,
                        onChange = { draft = draft.copy(copies = it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SwitchRow(
                        title = "Cor",
                        subtitle = "Desativado imprime em preto e branco",
                        checked = draft.color,
                        onCheckedChange = { draft = draft.copy(color = it) }
                    )
                    SwitchRow(
                        title = "Frente e verso",
                        subtitle = "Frente e verso quando compatível com o dispositivo",
                        checked = draft.duplex,
                        onCheckedChange = { draft = draft.copy(duplex = it) }
                    )
                    DropdownChoice(
                        label = "Tamanho do papel",
                        options = listOf("A4", "Carta", "Ofício", "A3", "A5", "A6"),
                        selected = draft.mediaSize,
                        onSelected = { draft = draft.copy(mediaSize = it) }
                    )
                    DropdownChoice(
                        label = "Orientação",
                        options = listOf("Retrato", "Paisagem"),
                        selected = draft.orientation,
                        onSelected = { draft = draft.copy(orientation = it) }
                    )
                    SwitchRow(
                        title = "Todas as páginas",
                        subtitle = "Desative para definir um intervalo de páginas",
                        checked = draft.allPages,
                        onCheckedChange = { draft = draft.copy(allPages = it) }
                    )
                    if (!draft.allPages) {
                        androidx.compose.material3.OutlinedTextField(
                            value = draft.pageRange,
                            onValueChange = { draft = draft.copy(pageRange = it) },
                            label = { Text("Intervalo de páginas") },
                            placeholder = { Text("ex.: 1-3, 5") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPrintSettingsChange(draft)
                        showSettingsDialog = false
                    }
                ) { Text("Concluído") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun StepperRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onChange((value - 1).coerceAtLeast(1)) }) { Text("-") }
            Text(
                value.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.SemiBold
            )
            OutlinedButton(onClick = { onChange((value + 1).coerceAtMost(999)) }) { Text("+") }
        }
    }
}
