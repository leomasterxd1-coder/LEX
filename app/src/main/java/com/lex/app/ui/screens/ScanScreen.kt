package com.lex.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.JobKind
import com.lex.app.data.ScanSettings
import com.lex.app.platform.PdfUtils
import com.lex.app.platform.PrintController
import com.lex.app.platform.ScanUtils
import com.lex.app.ui.components.DropdownChoice
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanScreen(
    state: AppData,
    scanSettings: ScanSettings,
    onCreateJob: (String, JobKind, Int, Boolean, Boolean, String) -> Unit,
    onScanSettingsChange: (ScanSettings) -> Unit,
    onOpenDevices: () -> Unit
) {
    val context = LocalContext.current
    var pages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val camera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { pages = pages + it }
        } else {
            Toast.makeText(context, "The photo was not captured.", Toast.LENGTH_SHORT).show()
        }
        pendingCameraUri = null
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) pages = pages + uris
    }

    fun capture() {
        val uri = ScanUtils.newCaptureUri(context)
        pendingCameraUri = uri
        runCatching { camera.launch(uri) }.onFailure {
            Toast.makeText(context, "No camera app is available.", Toast.LENGTH_SHORT).show()
        }
    }

    fun simulated() {
        pages = pages + PdfUtils.createSimulatedScan(context)
    }

    val scanner = state.defaultDevice
    val printSettings = state.printSettings

    fun savePdf() {
        if (pages.isEmpty()) {
            Toast.makeText(context, "Add at least one page first.", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val pdf = PdfUtils.pdfFromUris(context, pages)
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            PdfUtils.savePdfToDownloads(context, pdf, "LEX_Scan_$stamp.pdf")
        }.onSuccess { uri ->
            if (uri != null) {
                onCreateJob(
                    "Scan ${pages.size} page(s)",
                    JobKind.SCAN,
                    pages.size,
                    scanSettings.colorMode != "Black and White",
                    false,
                    "Saved to Downloads/LEX"
                )
                Toast.makeText(context, "Saved to Downloads/LEX", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Could not save the PDF.", Toast.LENGTH_LONG).show()
            }
        }.onFailure {
            Toast.makeText(context, it.message ?: "Could not create the PDF.", Toast.LENGTH_LONG).show()
        }
    }

    fun printPages() {
        if (pages.isEmpty()) {
            Toast.makeText(context, "Add at least one page first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (scanner == null) {
            Toast.makeText(context, "Add a device before printing.", Toast.LENGTH_SHORT).show()
            return
        }
        PrintController.printUris(
            context,
            pages,
            "LEX Scan",
            printSettings.copy(color = scanSettings.colorMode != "Black and White")
        ).onSuccess {
            onCreateJob(
                "Scan ${pages.size} page(s)",
                JobKind.SCAN,
                pages.size,
                scanSettings.colorMode != "Black and White",
                false,
                "Sent to ${scanner.name}"
            )
            Toast.makeText(context, "Opening Android print dialog…", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, it.message ?: "Could not print.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = { LexTopBar(title = "Scan") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "Scanner",
                actionLabel = if (state.devices.isEmpty()) "Add device" else null,
                onAction = if (state.devices.isEmpty()) onOpenDevices else null
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Scanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            scanner?.name ?: "No scanner selected",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            scanner?.let {
                                if (it.supportsScan) "${it.model} · ${it.status.label}"
                                else "This device does not expose a scanner; camera and photos still work."
                            } ?: "Camera, photos and simulated scanning are available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SectionHeader(title = "Scan from")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = ::capture, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Camera")
                }
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Photos")
                }
            }
            OutlinedButton(onClick = ::simulated, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Scanner, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Simulated scanner page")
            }

            SectionHeader(
                title = "Pages (${pages.size})",
                actionLabel = if (pages.isNotEmpty()) "Clear" else null,
                onAction = if (pages.isNotEmpty()) {
                    { pages = emptyList() }
                } else null
            )
            if (pages.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No pages yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Capture a page with the camera, pick photos or use the simulated scanner.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                pages.forEachIndexed { index, uri ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PageThumbnail(uri)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Page ${index + 1}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Ready to save or print",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { pages = pages.filterIndexed { i, _ -> i != index } }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove page")
                            }
                        }
                    }
                }
            }

            SectionHeader(title = "Scan settings")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DropdownChoice(
                        label = "Scan type",
                        options = listOf("Color", "Black and White", "Grayscale"),
                        selected = scanSettings.colorMode,
                        onSelected = { onScanSettingsChange(scanSettings.copy(colorMode = it)) }
                    )
                    DropdownChoice(
                        label = "Resolution",
                        options = listOf("75 dpi", "150 dpi", "200 dpi", "300 dpi", "400 dpi", "600 dpi", "1200 dpi"),
                        selected = "${scanSettings.dpi} dpi",
                        onSelected = {
                            onScanSettingsChange(
                                scanSettings.copy(dpi = it.substringBefore(" ").toIntOrNull() ?: 300)
                            )
                        }
                    )
                    DropdownChoice(
                        label = "Original size",
                        options = listOf("Auto Detect", "A4", "Letter", "Legal", "A3", "A5", "A6"),
                        selected = scanSettings.originalSize,
                        onSelected = { onScanSettingsChange(scanSettings.copy(originalSize = it)) }
                    )
                    DropdownChoice(
                        label = "Sides",
                        options = listOf("One-Sided", "Two-Sided (Long Edge)", "Two-Sided (Short Edge)"),
                        selected = scanSettings.sides,
                        onSelected = { onScanSettingsChange(scanSettings.copy(sides = it)) }
                    )
                }
            }

            Button(
                onClick = ::savePdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 6.dp))
                Text("Save PDF")
            }
            OutlinedButton(
                onClick = ::printPages,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Print, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 6.dp))
                Text("Print scan")
            }
            OutlinedButton(onClick = ::simulated, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Add simulated page")
            }
        }
    }
}

@Composable
private fun PageThumbnail(uri: Uri) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            PdfUtils.decodeSampledBitmap(context, uri)?.asImageBitmap()
        }
    }
    Box(
        modifier = Modifier
            .size(58.dp)
            .then(
                Modifier.padding(0.dp)
            )
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(58.dp)
                    .padding(0.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("…", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
