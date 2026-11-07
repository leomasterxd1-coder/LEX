package com.lex.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.BuildConfig
import com.lex.app.data.AppData
import com.lex.app.data.Device
import com.lex.app.data.PrintSettings
import com.lex.app.data.ScanSettings
import com.lex.app.data.Source
import com.lex.app.data.SourceType
import com.lex.app.ui.components.DropdownChoice
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader
import com.lex.app.ui.components.SwitchRow

@Composable
fun MoreScreen(
    state: AppData,
    onOpenDevices: () -> Unit,
    onOpenSources: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWhatsNew: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDataCollection: () -> Unit,
    onOpenOpenSource: () -> Unit
) {
    Scaffold(topBar = { LexTopBar(title = "More") }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "LEX ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "${state.devices.size} device(s) · ${state.jobs.size} job(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            item { MoreRow(Icons.Filled.Devices, "Manage devices", "Add, discover and remove printers", onOpenDevices) }
            item { MoreRow(Icons.Filled.FolderOpen, "Manage sources", "Files, photos, camera, web and cloud", onOpenSources) }
            item { MoreRow(Icons.Filled.History, "Print queue", "Held jobs and release", onOpenQueue) }
            item { MoreRow(Icons.Filled.Input, "Import data", "Bring devices from another app", onOpenImport) }
            item { MoreRow(Icons.Filled.Settings, "Settings", "Print and scan defaults", onOpenSettings) }
            item { MoreRow(Icons.Filled.PrivacyTip, "Data collection", "Privacy controls", onOpenDataCollection) }
            item { MoreRow(Icons.Filled.NewReleases, "What's new", "Release notes for LEX", onOpenWhatsNew) }
            item { MoreRow(Icons.Filled.Code, "Open source licenses", "Libraries used by LEX", onOpenOpenSource) }
            item { MoreRow(Icons.Filled.Info, "About LEX", "Version and legal notice", onOpenAbout) }
            item { MoreRow(Icons.Filled.HelpOutline, "Help", "Connection and printing tips", onOpenAbout) }
            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    "LEX is an independent clean-room project. It is not affiliated with, endorsed by or sponsored by Lexmark.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MoreRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.padding(horizontal = 8.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: AppData,
    onBack: () -> Unit,
    onPrintSettingsChange: (PrintSettings) -> Unit,
    onScanSettingsChange: (ScanSettings) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onDataCollectionChange: (Boolean) -> Unit,
    onReset: () -> Unit
) {
    var showReset by remember { mutableStateOf(false) }

    Scaffold(topBar = { LexTopBar(title = "Settings", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(title = "Print defaults")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NumberStepper(
                        label = "Copies",
                        value = state.printSettings.copies,
                        onChange = { onPrintSettingsChange(state.printSettings.copy(copies = it)) }
                    )
                    SwitchRow(
                        title = "Color",
                        subtitle = "Use color when the printer supports it",
                        checked = state.printSettings.color,
                        onCheckedChange = {
                            onPrintSettingsChange(state.printSettings.copy(color = it))
                        }
                    )
                    SwitchRow(
                        title = "Two-sided",
                        subtitle = "Print on both sides of the paper",
                        checked = state.printSettings.duplex,
                        onCheckedChange = {
                            onPrintSettingsChange(state.printSettings.copy(duplex = it))
                        }
                    )
                    DropdownChoice(
                        label = "Media size",
                        options = listOf("A4", "Letter", "Legal", "A3", "A5", "A6"),
                        selected = state.printSettings.mediaSize,
                        onSelected = {
                            onPrintSettingsChange(state.printSettings.copy(mediaSize = it))
                        }
                    )
                    DropdownChoice(
                        label = "Orientation",
                        options = listOf("Portrait", "Landscape"),
                        selected = state.printSettings.orientation,
                        onSelected = {
                            onPrintSettingsChange(state.printSettings.copy(orientation = it))
                        }
                    )
                }
            }

            SectionHeader(title = "Scan defaults")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DropdownChoice(
                        label = "Scan type",
                        options = listOf("Color", "Black and White", "Grayscale"),
                        selected = state.scanSettings.colorMode,
                        onSelected = {
                            onScanSettingsChange(state.scanSettings.copy(colorMode = it))
                        }
                    )
                    DropdownChoice(
                        label = "Resolution",
                        options = listOf("150 dpi", "200 dpi", "300 dpi", "400 dpi", "600 dpi"),
                        selected = "${state.scanSettings.dpi} dpi",
                        onSelected = {
                            onScanSettingsChange(
                                state.scanSettings.copy(
                                    dpi = it.substringBefore(" ").toIntOrNull() ?: 300
                                )
                            )
                        }
                    )
                    DropdownChoice(
                        label = "Sides",
                        options = listOf("One-Sided", "Two-Sided (Long Edge)", "Two-Sided (Short Edge)"),
                        selected = state.scanSettings.sides,
                        onSelected = {
                            onScanSettingsChange(state.scanSettings.copy(sides = it))
                        }
                    )
                }
            }

            SectionHeader(title = "Privacy and notifications")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SwitchRow(
                        title = "Job notifications",
                        subtitle = "Show a notification when a job completes",
                        checked = state.notifications,
                        onCheckedChange = onNotificationsChange
                    )
                    SwitchRow(
                        title = "Anonymous usage data",
                        subtitle = "Optional diagnostics, off by default",
                        checked = state.dataCollection,
                        onCheckedChange = onDataCollectionChange
                    )
                }
            }

            SectionHeader(title = "Reset")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Reset LEX to onboarding. Devices, jobs and preferences will be cleared.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { showReset = true }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("Reset LEX")
                    }
                }
            }

            Text(
                "Theme: LEX Red · arm64-v8a release",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset LEX?") },
            text = { Text("This clears all local devices, jobs, history and settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReset = false
                        onReset()
                    }
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun NumberStepper(label: String, value: Int, onChange: (Int) -> Unit) {
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

@Composable
fun AboutScreen(onBack: () -> Unit, onOpenOpenSource: () -> Unit) {
    Scaffold(topBar = { LexTopBar(title = "About LEX", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(72.dp)
            )
            Text(
                "LEX",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "LEX is a clean-room Jetpack Compose app inspired by mobile print workflows. It recreates the experience with original code and assets.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "LEX is not affiliated with, endorsed by or sponsored by Lexmark. Lexmark is a trademark of its respective owner.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Build: arm64-v8a · minSdk 29 · targetSdk 36",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onOpenOpenSource, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Code, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Open source licenses")
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "© 2026 LEX contributors",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WhatsNewScreen(onBack: () -> Unit) {
    Scaffold(topBar = { LexTopBar(title = "What's new", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "LEX 1.0.0",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Bullet("Red LEX theme implemented with Material 3.")
            Bullet("Print files, photos and web pages through the Android print framework.")
            Bullet("Scan with camera, photo picker or a simulated scanner page.")
            Bullet("Discover compatible printers with NSD/mDNS and add devices manually.")
            Bullet("Queue, release and track print, copy, scan and web jobs.")
            Bullet("Import devices from JSON and manage sources, privacy and defaults.")
            Bullet("arm64-v8a release built automatically on GitHub Actions.")
        }
    }
}

@Composable
fun DataCollectionScreen(
    state: AppData,
    onBack: () -> Unit,
    onDataCollectionChange: (Boolean) -> Unit
) {
    Scaffold(topBar = { LexTopBar(title = "Data collection", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SwitchRow(
                        title = "Anonymous usage data",
                        subtitle = "Share optional diagnostics to improve LEX",
                        checked = state.dataCollection,
                        onCheckedChange = onDataCollectionChange
                    )
                }
            }
            Text(
                "LEX stores devices, jobs, history and preferences only on this device. No account is required and no document content is uploaded by LEX.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "When enabled, anonymous usage data would be limited to feature counts and crash diagnostics. It is disabled by default.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OpenSourceScreen(onBack: () -> Unit) {
    Scaffold(topBar = { LexTopBar(title = "Open source", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "LEX is built with open source software:",
                style = MaterialTheme.typography.bodyMedium
            )
            LicenseItem("Kotlin", "Apache License 2.0")
            LicenseItem("Jetpack Compose", "Apache License 2.0")
            LicenseItem("AndroidX / Material 3", "Apache License 2.0")
            LicenseItem("Navigation Compose", "Apache License 2.0")
            LicenseItem("Kotlin Coroutines", "Apache License 2.0")
            LicenseItem("AndroidX DocumentFile", "Apache License 2.0")
            Text(
                "Android WebView and the Android print framework are part of Android and are used under their respective platform terms.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicenseItem(name: String, license: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(
                license,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImportDataScreen(
    onBack: () -> Unit,
    importJson: (String) -> Result<Int>,
    exportJson: () -> String
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.onSuccess { content ->
                text = content.orEmpty()
            }.onFailure {
                Toast.makeText(context, "Could not read the file.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(topBar = { LexTopBar(title = "Import data", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Import saved devices and preferences from another print app. LEX accepts a JSON list with name, model, address and type.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = { Text("Devices JSON") },
                placeholder = { Text("[{\"name\":\"Printer\",\"address\":\"192.168.1.50\"}]") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { filePicker.launch(arrayOf("application/json", "text/*")) }) {
                    Text("Choose file")
                }
                OutlinedButton(onClick = { text = exportJson() }) {
                    Text("Export current")
                }
                OutlinedButton(onClick = {
                    text = "[\n  {\"name\":\"Office Printer\",\"address\":\"192.168.1.50\",\"type\":\"MFP\"}\n]"
                }) {
                    Text("Sample")
                }
            }
            Button(
                onClick = {
                    importJson(text)
                        .onSuccess {
                            Toast.makeText(
                                context,
                                "$it device(s) imported.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .onFailure {
                            Toast.makeText(
                                context,
                                it.message ?: "Import failed.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Import devices") }
            Text(
                "Imported devices are stored locally. No data leaves your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SourcesScreen(
    state: AppData,
    onBack: () -> Unit,
    onToggleSource: (String, Boolean) -> Unit
) {
    Scaffold(topBar = { LexTopBar(title = "Sources", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Choose which sources are available when printing or scanning.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.sources.forEach { source ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            sourceIcon(source.type),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.padding(horizontal = 8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(source.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                source.detail.ifBlank { source.type.label },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = source.enabled,
                            onCheckedChange = { onToggleSource(source.id, it) }
                        )
                    }
                }
            }
            Text(
                "Cloud sources are a demo placeholder in this clean-room build. Local files, photos, camera and web printing work through Android APIs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun sourceIcon(type: SourceType): ImageVector = when (type) {
    SourceType.LOCAL -> Icons.Filled.Storage
    SourceType.PHOTOS -> Icons.Filled.PhotoLibrary
    SourceType.CAMERA -> Icons.Filled.PhotoCamera
    SourceType.WEB -> Icons.Filled.Language
    SourceType.CLOUD -> Icons.Filled.Cloud
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
