package com.lex.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lex.app.data.Device
import com.lex.app.data.DeviceStatus
import com.lex.app.ui.theme.LexAmber
import com.lex.app.ui.theme.LexGreen
import com.lex.app.ui.theme.LexRed

private data class SourceTile(
    val label: String,
    val icon: ImageVector,
    val tint: Color
)

@Composable
fun HomeScreen(
    device: Device?,
    onOpenDevice: (String) -> Unit,
    onFindDevices: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenPhotos: () -> Unit,
    onOpenFiles: () -> Unit,
    onOpenWeb: () -> Unit,
    onOpenScanner: () -> Unit,
    onEditSources: () -> Unit
) {
    val tiles = listOf(
        SourceTile("Câmera", Icons.Filled.PhotoCamera, Color(0xFF5C5C5C)),
        SourceTile("Fotografias", Icons.Filled.PhotoLibrary, Color(0xFF2E7D32)),
        SourceTile("Arquivos", Icons.Filled.Folder, Color(0xFF2E7D32)),
        SourceTile("Web", Icons.Filled.Language, Color(0xFF1565C0)),
        SourceTile("Digitalizar", Icons.Filled.Scanner, Color(0xFF2E7D32))
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        bottomBar = {
            if (device != null) {
                SelectedDeviceBar(device = device, onClick = { onOpenDevice(device.id) })
            }
        },
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4A4A4A))
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "LEX",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        " | Impressão",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Fontes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onEditSources) {
                        Text(
                            "Editar",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tiles, key = { it.label }) { tile ->
                SourceTileCard(tile) {
                    when (tile.label) {
                        "Câmera" -> onOpenCamera()
                        "Fotografias" -> onOpenPhotos()
                        "Arquivos" -> onOpenFiles()
                        "Web" -> onOpenWeb()
                        else -> onOpenScanner()
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceTileCard(tile: SourceTile, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = tile.label,
                tint = tile.tint,
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                tile.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun SelectedDeviceBar(device: Device, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4A4A4A))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Dispositivo selecionado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(Modifier.height(3.dp))
            Text(
                device.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor(device.status), CircleShape)
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text(
                    device.status.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
        Box(contentAlignment = Alignment.BottomStart) {
            Icon(
                Icons.Filled.Print,
                contentDescription = null,
                tint = Color(0xFFD6D6D6),
                modifier = Modifier.size(64.dp)
            )
            if (device.isDefault) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Dispositivo padrão",
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Abrir dispositivo",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

private fun statusColor(status: DeviceStatus): Color = when (status) {
    DeviceStatus.READY -> LexGreen
    DeviceStatus.WARNING -> LexAmber
    DeviceStatus.OFFLINE -> Color.Gray
    DeviceStatus.ERROR -> LexRed
}
