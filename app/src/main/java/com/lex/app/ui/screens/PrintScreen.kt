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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun PrintScreen(
    state: AppData,
    onCreateJob: (String, JobKind, Int, Boolean, Boolean, String) -> Unit,
    onPrintSettingsChange: (PrintSettings) -> Unit,
    onSelectDevice: (String) -> Unit,
    onOpenDevices: () -> Unit,
    onOpenWeb: () -> Unit,
    onOpenScan: () -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("") }
    var selectedMeta by remember { mutableStateOf("") }
    var selectedDeviceId by remember { mutableStateOf(state.defaultDevice?.id) }
    var showSettings by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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
                context.contentResolver.getType(uri) ?: "document",
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
                context.contentResolver.getType(uri) ?: "image",
                PdfUtils.sizeLabel(context, uri)
            ).filter { it.isNotBlank() }.joinToString(" · ")
        }
    }

    val selectedDevice = state.device(selectedDeviceId) ?: state.defaultDevice
    val printSettings = state.printSettings

    fun performPrint() {
        val uri = selectedUri
        if (uri == null) {
            Toast.makeText(context, "Choose a file or photo first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDevice == null) {
            Toast.makeText(context, "Add or select a device first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!selectedDevice.supportsPrint) {
            Toast.makeText(context, "This device does not support printing.", Toast.LENGTH_SHORT).show()
            return
        }
        PrintController.printUri(context, uri, selectedName.ifBlank { "Document" }, printSettings)
            .onSuccess {
                onCreateJob(
                    selectedName.ifBlank { "Document" },
                    JobKind.PRINT,
                    printSettings.copies,
                    printSettings.color,
                    printSettings.duplex,
                    "Sent to ${selectedDevice.name}"
                )
                Toast.makeText(context, "Opening Android print dialog…", Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(
                    context,
                    it.message ?: "Could not start the print job.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    Scaffold(
        topBar = {
            LexTopBar(title = "Print")
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
            SectionHeader(title = "Printer")
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
                                "No devices yet",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Discover a printer or add the demo device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Button(onClick = onOpenDevices) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Add")
                        }
                    }
                }
            } else {
                DropdownChoice(
                    label = "Selected device",
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

            SectionHeader(title = "Document")
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
                                selectedName.ifBlank { "No file selected" },
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                selectedMeta.ifBlank {
                                    "PDF, images and text files are supported."
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
                        ) { Text("Choose file") }
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
                            Text("Photo")
                        }
                    }
                }
            }

            SectionHeader(
                title = "Print settings",
                actionLabel = "Change",
                onAction = { showSettingsDialog = true }
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Copies", printSettings.copies.toString())
                    InfoRow("Color", if (printSettings.color) "Color" else "Black and White")
                    InfoRow("Sides", if (printSettings.duplex) "Two-Sided" else "One-Sided")
                    InfoRow("Media size", printSettings.mediaSize)
                    InfoRow("Orientation", printSettings.orientation)
                    InfoRow(
                        "Pages",
                        if (printSettings.allPages) "All pages" else printSettings.pageRange.ifBlank { "Custom" }
                    )
                }
            }

            Button(
                onClick = { performPrint() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Print, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 6.dp))
                Text("Print")
            }

            SectionHeader(title = "More ways to print")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onOpenWeb, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Language, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Web page")
                }
                OutlinedButton(onClick = onOpenScan, modifier = Modifier.weight(1f)) {
                    Text("Scan first")
                }
            }
        }
    }

    if (showSettingsDialog) {
        var draft by remember(printSettings) { mutableStateOf(printSettings) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Print settings", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    StepperRow(
                        label = "Copies",
                        value = draft.copies,
                        onChange = { draft = draft.copy(copies = it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SwitchRow(
                        title = "Color",
                        subtitle = "Off prints in black and white",
                        checked = draft.color,
                        onCheckedChange = { draft = draft.copy(color = it) }
                    )
                    SwitchRow(
                        title = "Two-sided",
                        subtitle = "Duplex when supported by the device",
                        checked = draft.duplex,
                        onCheckedChange = { draft = draft.copy(duplex = it) }
                    )
                    DropdownChoice(
                        label = "Media size",
                        options = listOf("A4", "Letter", "Legal", "A3", "A5", "A6"),
                        selected = draft.mediaSize,
                        onSelected = { draft = draft.copy(mediaSize = it) }
                    )
                    DropdownChoice(
                        label = "Orientation",
                        options = listOf("Portrait", "Landscape"),
                        selected = draft.orientation,
                        onSelected = { draft = draft.copy(orientation = it) }
                    )
                    SwitchRow(
                        title = "All pages",
                        subtitle = "Turn off to set a page range",
                        checked = draft.allPages,
                        onCheckedChange = { draft = draft.copy(allPages = it) }
                    )
                    if (!draft.allPages) {
                        androidx.compose.material3.OutlinedTextField(
                            value = draft.pageRange,
                            onValueChange = { draft = draft.copy(pageRange = it) },
                            label = { Text("Page range") },
                            placeholder = { Text("e.g. 1-3, 5") },
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
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Cancel") }
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
