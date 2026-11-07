package com.lex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.JobStatus
import com.lex.app.ui.components.ActionCard
import com.lex.app.ui.components.DeviceCard
import com.lex.app.ui.components.JobCard
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader

@Composable
fun HomeScreen(
    state: AppData,
    onOpenPrint: () -> Unit,
    onOpenScan: () -> Unit,
    onOpenCopy: () -> Unit,
    onOpenWeb: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDevice: (String) -> Unit,
    onAddDemoDevice: () -> Unit
) {
    Scaffold(
        topBar = {
            LexTopBar(
                title = "LEX",
                actions = {
                    IconButton(onClick = onOpenQueue) {
                        Icon(Icons.Filled.History, contentDescription = "Queue")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        "Ready to print",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Print from files, photos, the web and more.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val defaultDevice = state.defaultDevice
            if (defaultDevice != null) {
                item {
                    SectionHeader(
                        title = "Default device",
                        actionLabel = "Manage",
                        onAction = onOpenDevices
                    )
                }
                item {
                    DeviceCard(
                        device = defaultDevice,
                        onClick = { onOpenDevice(defaultDevice.id) }
                    )
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Add your first device",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Use a demo printer or discover printers on your network.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onAddDemoDevice) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(Modifier.padding(horizontal = 4.dp))
                                    Text("Demo")
                                }
                                TextButton(onClick = onOpenDevices) { Text("Find devices") }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Quick actions")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        icon = Icons.Filled.Print,
                        title = "Print",
                        subtitle = "Files, photos and PDFs",
                        onClick = onOpenPrint,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        icon = Icons.Filled.Scanner,
                        title = "Scan",
                        subtitle = "Camera or photos",
                        onClick = onOpenScan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        icon = Icons.Filled.ContentCopy,
                        title = "Copy",
                        subtitle = "Scan and print",
                        onClick = onOpenCopy,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        icon = Icons.Filled.Language,
                        title = "Web print",
                        subtitle = "Print a page from the web",
                        onClick = onOpenWeb,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Recent jobs",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onOpenQueue) { Text("View all") }
                }
            }

            val recent = state.jobs.take(3)
            if (recent.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Devices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.padding(horizontal = 6.dp))
                            Column {
                                Text("No jobs yet", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Your print and scan jobs will appear here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(recent, key = { it.id }) { job ->
                    JobCard(job = job)
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                                "Print from anywhere",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Files, photos, web pages, camera and other apps.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.jobs.any { it.status == JobStatus.PROCESSING }) {
                            Text(
                                "Processing…",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
