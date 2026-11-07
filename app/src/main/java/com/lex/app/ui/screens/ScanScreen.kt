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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScanScreen(
    state: AppData,
    scanSettings: ScanSettings,
    onCreateJob: (String, JobKind, Int, Boolean, Boolean, String) -> Unit,
    onScanSettingsChange: (ScanSettings) -> Unit,
    onBack: () -> Unit,
    onOpenDevices: () -> Unit
) {
    val context = LocalContext.current
    var pages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val camera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { pages = pages + it }
        } else {
            Toast.makeText(context, "A foto não foi capturada.", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Nenhum aplicativo de câmera disponível.", Toast.LENGTH_SHORT).show()
        }
    }

    fun simulated() {
        if (busy) return
        busy = true
        scope.launch {
            val uri = withContext(Dispatchers.IO) { PdfUtils.createSimulatedScan(context) }
            pages = pages + uri
            busy = false
        }
    }

    val scanner = state.defaultDevice
    val printSettings = state.printSettings

    fun savePdf() {
        if (pages.isEmpty()) {
            Toast.makeText(context, "Adicione pelo menos uma página primeiro.", Toast.LENGTH_SHORT).show()
            return
        }
        if (busy) return
        val snapshot = pages
        busy = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val pdf = PdfUtils.pdfFromUris(context, snapshot)
                    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    PdfUtils.savePdfToDownloads(context, pdf, "LEX_Scan_$stamp.pdf")
                }
            }
            result.onSuccess { uri ->
                if (uri != null) {
                    onCreateJob(
                        "Digitalização de ${snapshot.size} página(s)",
                        JobKind.SCAN,
                        snapshot.size,
                        scanSettings.colorMode != "Preto e branco",
                        false,
                        "Salvo em Downloads/LEX"
                    )
                    Toast.makeText(context, "Salvo em Downloads/LEX", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Não foi possível salvar o PDF.", Toast.LENGTH_LONG).show()
                }
            }.onFailure {
                Toast.makeText(context, it.message ?: "Não foi possível criar o PDF.", Toast.LENGTH_LONG).show()
            }
            busy = false
        }
    }

    fun printPages() {
        if (pages.isEmpty()) {
            Toast.makeText(context, "Adicione pelo menos uma página primeiro.", Toast.LENGTH_SHORT).show()
            return
        }
        if (scanner == null) {
            Toast.makeText(context, "Adicione um dispositivo antes de imprimir.", Toast.LENGTH_SHORT).show()
            return
        }
        if (busy) return
        val snapshot = pages
        busy = true
        scope.launch {
            PrintController.printUris(
                context,
                snapshot,
                scanner,
                "Digitalização LEX",
                printSettings.copy(color = scanSettings.colorMode != "Preto e branco")
            ).onSuccess {
                onCreateJob(
                    "Digitalização de ${snapshot.size} página(s)",
                    JobKind.SCAN,
                    snapshot.size,
                    scanSettings.colorMode != "Preto e branco",
                    false,
                    "Enviado para ${scanner.address}"
                )
                Toast.makeText(context, "Enviado diretamente para ${scanner.address}", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, it.message ?: "Não foi possível imprimir.", Toast.LENGTH_LONG).show()
            }
            busy = false
        }
    }

    Scaffold(
        topBar = { LexTopBar(title = "Digitalizar", onBack = onBack) }
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
                actionLabel = if (state.devices.isEmpty()) "Adicionar dispositivo" else null,
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
                            scanner?.name ?: "Nenhum scanner selecionado",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            scanner?.let {
                                if (it.supportsScan) "${it.model} · ${it.status.label}"
                                else "Este dispositivo não expõe um scanner; câmera e fotos continuam funcionando."
                            } ?: "Câmera, fotos e digitalização simulada estão disponíveis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SectionHeader(title = "Digitalizar de")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = ::capture, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Câmera")
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
                    Text("Fotos")
                }
            }
            OutlinedButton(onClick = ::simulated, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Scanner, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Página de scanner simulada")
            }

            SectionHeader(
                title = "Páginas (${pages.size})",
                actionLabel = if (pages.isNotEmpty()) "Limpar" else null,
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
                            "Nenhuma página ainda",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Capture uma página com a câmera, escolha fotos ou use o scanner simulado.",
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
                                    "Página ${index + 1}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Pronto para salvar ou imprimir",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { pages = pages.filterIndexed { i, _ -> i != index } }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover página")
                            }
                        }
                    }
                }
            }

            SectionHeader(title = "Configurações de digitalização")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DropdownChoice(
                        label = "Tipo de digitalização",
                        options = listOf("Colorido", "Preto e branco", "Escala de cinza"),
                        selected = scanSettings.colorMode,
                        onSelected = { onScanSettingsChange(scanSettings.copy(colorMode = it)) }
                    )
                    DropdownChoice(
                        label = "Resolução",
                        options = listOf("75 dpi", "150 dpi", "200 dpi", "300 dpi", "400 dpi", "600 dpi", "1200 dpi"),
                        selected = "${scanSettings.dpi} dpi",
                        onSelected = {
                            onScanSettingsChange(
                                scanSettings.copy(dpi = it.substringBefore(" ").toIntOrNull() ?: 300)
                            )
                        }
                    )
                    DropdownChoice(
                        label = "Tamanho original",
                        options = listOf("Detecção automática", "A4", "Carta", "Ofício", "A3", "A5", "A6"),
                        selected = scanSettings.originalSize,
                        onSelected = { onScanSettingsChange(scanSettings.copy(originalSize = it)) }
                    )
                    DropdownChoice(
                        label = "Lados",
                        options = listOf("Um lado", "Frente e verso (borda longa)", "Frente e verso (borda curta)"),
                        selected = scanSettings.sides,
                        onSelected = { onScanSettingsChange(scanSettings.copy(sides = it)) }
                    )
                }
            }

            Button(
                onClick = ::savePdf,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Text("Salvar PDF")
                }
            }
            OutlinedButton(
                onClick = ::printPages,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Print, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Text("Imprimir digitalização")
                }
            }
            OutlinedButton(
                onClick = ::simulated,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Adicionar página simulada")
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
