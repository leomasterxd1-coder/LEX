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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.JobKind
import com.lex.app.platform.PdfUtils
import com.lex.app.platform.PrintController
import com.lex.app.platform.ScanUtils
import com.lex.app.ui.components.InfoRow
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable
fun CopyScreen(
    state: AppData,
    onCreateJob: (String, JobKind, Int, Boolean, Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var page by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var copying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val camera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) page = pendingCameraUri
        pendingCameraUri = null
    }

    val photos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) page = uri }

    fun capture() {
        val uri = ScanUtils.newCaptureUri(context)
        pendingCameraUri = uri
        runCatching { camera.launch(uri) }.onFailure {
            Toast.makeText(context, "Nenhum aplicativo de câmera disponível.", Toast.LENGTH_SHORT).show()
        }
    }

    fun copy() {
        val uri = page ?: run {
            Toast.makeText(context, "Escolha ou capture uma página primeiro.", Toast.LENGTH_SHORT).show()
            return
        }
        val device = state.defaultDevice ?: run {
            Toast.makeText(context, "Adicione um dispositivo primeiro.", Toast.LENGTH_SHORT).show()
            return
        }
        if (copying) return
        copying = true
        scope.launch {
            PrintController.printUri(context, uri, device, "Cópia LEX", state.printSettings)
                .onSuccess {
                    onCreateJob(
                        "Copiar",
                        JobKind.COPY,
                        state.printSettings.copies,
                        state.printSettings.color,
                        state.printSettings.duplex,
                        "Enviado para ${device.address}"
                    )
                    Toast.makeText(context, "Enviado diretamente para ${device.address}", Toast.LENGTH_LONG).show()
                }
                .onFailure {
                    Toast.makeText(context, it.message ?: "Não foi possível copiar.", Toast.LENGTH_LONG).show()
                }
            copying = false
        }
    }

    Scaffold(topBar = { LexTopBar(title = "Copiar", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(title = "Coloque o original")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val preview = page
                    if (preview != null) {
                        UriImage(preview, modifier = Modifier.size(180.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = ::capture) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Câmera")
                        }
                        OutlinedButton(
                            onClick = {
                                photos.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Fotos")
                        }
                    }
                }
            }

            SectionHeader(title = "Configurações de cópia")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Cópias", state.printSettings.copies.toString())
                    InfoRow("Cor", if (state.printSettings.color) "Colorido" else "Preto e branco")
                    InfoRow(
                        "Lados",
                        if (state.printSettings.duplex) "Frente e verso" else "Um lado"
                    )
                    InfoRow("Tamanho do papel", state.printSettings.mediaSize)
                }
            }

            Button(
                onClick = ::copy,
                enabled = !copying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (copying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Text("Copiar")
                }
            }
        }
    }
}

@Composable
private fun UriImage(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            PdfUtils.decodeSampledBitmap(context, uri)?.asImageBitmap()
        }
    }
    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    }
}
