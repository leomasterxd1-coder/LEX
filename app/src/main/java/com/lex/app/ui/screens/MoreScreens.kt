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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    Scaffold(topBar = { LexTopBar(title = "Mais") }) { innerPadding ->
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
                            "${state.devices.size} dispositivo(s) · ${state.jobs.size} trabalho(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            item { MoreRow(Icons.Filled.Devices, "Gerenciar dispositivos", "Adicione, descubra e remova impressoras", onOpenDevices) }
            item { MoreRow(Icons.Filled.FolderOpen, "Gerenciar fontes", "Arquivos, fotos, câmera, web e nuvem", onOpenSources) }
            item { MoreRow(Icons.Filled.History, "Fila de impressão", "Trabalhos retidos e liberação", onOpenQueue) }
            item { MoreRow(Icons.Filled.Input, "Importar dados", "Traga dispositivos de outro aplicativo", onOpenImport) }
            item { MoreRow(Icons.Filled.Settings, "Configurações", "Padrões de impressão e digitalização", onOpenSettings) }
            item { MoreRow(Icons.Filled.PrivacyTip, "Coleta de dados", "Controles de privacidade", onOpenDataCollection) }
            item { MoreRow(Icons.Filled.NewReleases, "Novidades", "Notas de versão do LEX", onOpenWhatsNew) }
            item { MoreRow(Icons.Filled.Code, "Licenças de código aberto", "Bibliotecas usadas pelo LEX", onOpenOpenSource) }
            item { MoreRow(Icons.Filled.Info, "Sobre o LEX", "Versão e aviso legal", onOpenAbout) }
            item { MoreRow(Icons.Filled.HelpOutline, "Ajuda", "Dicas de conexão e impressão", onOpenAbout) }
            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    "O LEX é um projeto independente, criado do zero. Não é afiliado, endossado ou patrocinado pela Lexmark.",
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

    Scaffold(topBar = { LexTopBar(title = "Configurações", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(title = "Padrões de impressão")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NumberStepper(
                        label = "Cópias",
                        value = state.printSettings.copies,
                        onChange = { onPrintSettingsChange(state.printSettings.copy(copies = it)) }
                    )
                    SwitchRow(
                        title = "Cor",
                        subtitle = "Usar cor quando a impressora suportar",
                        checked = state.printSettings.color,
                        onCheckedChange = {
                            onPrintSettingsChange(state.printSettings.copy(color = it))
                        }
                    )
                    SwitchRow(
                        title = "Frente e verso",
                        subtitle = "Imprimir nos dois lados do papel",
                        checked = state.printSettings.duplex,
                        onCheckedChange = {
                            onPrintSettingsChange(state.printSettings.copy(duplex = it))
                        }
                    )
                    DropdownChoice(
                        label = "Tamanho do papel",
                        options = listOf("A4", "Carta", "Ofício", "A3", "A5", "A6"),
                        selected = state.printSettings.mediaSize,
                        onSelected = {
                            onPrintSettingsChange(state.printSettings.copy(mediaSize = it))
                        }
                    )
                    DropdownChoice(
                        label = "Orientação",
                        options = listOf("Retrato", "Paisagem"),
                        selected = state.printSettings.orientation,
                        onSelected = {
                            onPrintSettingsChange(state.printSettings.copy(orientation = it))
                        }
                    )
                }
            }

            SectionHeader(title = "Padrões de digitalização")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DropdownChoice(
                        label = "Tipo de digitalização",
                        options = listOf("Colorido", "Preto e branco", "Escala de cinza"),
                        selected = state.scanSettings.colorMode,
                        onSelected = {
                            onScanSettingsChange(state.scanSettings.copy(colorMode = it))
                        }
                    )
                    DropdownChoice(
                        label = "Resolução",
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
                        label = "Lados",
                        options = listOf("Um lado", "Frente e verso (borda longa)", "Frente e verso (borda curta)"),
                        selected = state.scanSettings.sides,
                        onSelected = {
                            onScanSettingsChange(state.scanSettings.copy(sides = it))
                        }
                    )
                }
            }

            SectionHeader(title = "Privacidade e notificações")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SwitchRow(
                        title = "Notificações de trabalhos",
                        subtitle = "Mostrar uma notificação quando um trabalho terminar",
                        checked = state.notifications,
                        onCheckedChange = onNotificationsChange
                    )
                    SwitchRow(
                        title = "Dados de uso anônimos",
                        subtitle = "Diagnósticos opcionais, desativados por padrão",
                        checked = state.dataCollection,
                        onCheckedChange = onDataCollectionChange
                    )
                }
            }

            SectionHeader(title = "Redefinir")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Redefinir o LEX para a tela inicial. Dispositivos, trabalhos e preferências serão apagados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { showReset = true }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("Redefinir LEX")
                    }
                }
            }

            Text(
                "Tema: vermelho vivo · compilação arm64-v8a",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Redefinir o LEX?") },
            text = { Text("Isso apaga todos os dispositivos, trabalhos, histórico e configurações locais.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReset = false
                        onReset()
                    }
                ) { Text("Redefinir") }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancelar") }
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
    Scaffold(topBar = { LexTopBar(title = "Sobre o LEX", onBack = onBack) }) { innerPadding ->
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
                "Versão ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "O LEX é um aplicativo Jetpack Compose independente inspirado em fluxos de impressão móvel. Ele recria a experiência com código e recursos originais.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "O LEX não é afiliado, endossado ou patrocinado pela Lexmark. Lexmark é uma marca registrada de seu respectivo proprietário.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Compilação: arm64-v8a · minSdk 29 · targetSdk 36",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onOpenOpenSource, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Code, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Licenças de código aberto")
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "© 2026 Contribuidores do LEX",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WhatsNewScreen(onBack: () -> Unit) {
    Scaffold(topBar = { LexTopBar(title = "Novidades", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "LEX 1.0.2",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Bullet("Tema vermelho vivo implementado com Material 3 e fundo branco na tela inicial.")
            Bullet("Dispositivo selecionado em destaque acima da navegação inferior.")
            Bullet("Geração de PDF e digitalização em segundo plano, com indicadores de progresso.")
            Bullet("APK otimizado com R8: muito menor e com inicialização mais rápida.")
            Bullet("Imprima arquivos, fotos e páginas da web pelo sistema de impressão do Android.")
            Bullet("Digitalize com a câmera, o seletor de fotos ou uma página de scanner simulada.")
            Bullet("Descubra impressoras compatíveis com NSD/mDNS e adicione dispositivos manualmente.")
            Bullet("Gerencie, libere e acompanhe trabalhos de impressão, cópia, digitalização e web.")
            Bullet("Importe dispositivos de JSON e gerencie fontes, privacidade e padrões.")
            Bullet("Compilação arm64-v8a gerada automaticamente no GitHub Actions.")
        }
    }
}

@Composable
fun DataCollectionScreen(
    state: AppData,
    onBack: () -> Unit,
    onDataCollectionChange: (Boolean) -> Unit
) {
    Scaffold(topBar = { LexTopBar(title = "Coleta de dados", onBack = onBack) }) { innerPadding ->
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
                        title = "Dados de uso anônimos",
                        subtitle = "Compartilhar diagnósticos opcionais para melhorar o LEX",
                        checked = state.dataCollection,
                        onCheckedChange = onDataCollectionChange
                    )
                }
            }
            Text(
                "O LEX armazena dispositivos, trabalhos, histórico e preferências apenas neste dispositivo. Não é necessário criar conta e nenhum conteúdo de documento é enviado pelo LEX.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Quando ativados, os dados anônimos se limitam à contagem de recursos e diagnósticos de falhas. Ficam desativados por padrão.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OpenSourceScreen(onBack: () -> Unit) {
    Scaffold(topBar = { LexTopBar(title = "Código aberto", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "O LEX é construído com software de código aberto:",
                style = MaterialTheme.typography.bodyMedium
            )
            LicenseItem("Kotlin", "Licença Apache 2.0")
            LicenseItem("Jetpack Compose", "Licença Apache 2.0")
            LicenseItem("AndroidX / Material 3", "Licença Apache 2.0")
            LicenseItem("Navigation Compose", "Licença Apache 2.0")
            LicenseItem("Kotlin Coroutines", "Licença Apache 2.0")
            LicenseItem("AndroidX DocumentFile", "Licença Apache 2.0")
            Text(
                "O WebView do Android e o sistema de impressão do Android fazem parte do Android e são usados sob os respectivos termos da plataforma.",
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
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val content = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                }
                content.onSuccess { text = it.orEmpty() }
                    .onFailure {
                        Toast.makeText(context, "Não foi possível ler o arquivo.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    Scaffold(topBar = { LexTopBar(title = "Importar dados", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Importe dispositivos e preferências salvos de outro aplicativo de impressão. O LEX aceita uma lista JSON com os campos name, model, address e type.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = { Text("JSON de dispositivos") },
                placeholder = { Text("[{\"name\":\"Impressora\",\"address\":\"192.168.1.50\"}]") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { filePicker.launch(arrayOf("application/json", "text/*")) }) {
                    Text("Escolher arquivo")
                }
                OutlinedButton(onClick = { text = exportJson() }) {
                    Text("Exportar atuais")
                }
                OutlinedButton(onClick = {
                    text = "[\n  {\"name\":\"Impressora do escritório\",\"address\":\"192.168.1.50\",\"type\":\"MFP\"}\n]"
                }) {
                    Text("Exemplo")
                }
            }
            Button(
                onClick = {
                    importJson(text)
                        .onSuccess {
                            Toast.makeText(
                                context,
                                "$it dispositivo(s) importado(s).",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .onFailure {
                            Toast.makeText(
                                context,
                                it.message ?: "Falha na importação.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Importar dispositivos") }
            Text(
                "Os dispositivos importados ficam armazenados localmente. Nenhum dado sai do seu dispositivo.",
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
    Scaffold(topBar = { LexTopBar(title = "Fontes", onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Escolha quais fontes estão disponíveis ao imprimir ou digitalizar.",
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
                "As fontes na nuvem são um espaço reservado para demonstração nesta versão independente. Arquivos locais, fotos, câmera e impressão web funcionam pelas APIs do Android.",
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
