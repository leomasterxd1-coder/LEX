package com.lex.app.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lex.app.data.AppData
import com.lex.app.data.JobKind
import com.lex.app.platform.PrintController
import com.lex.app.ui.components.LexTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPrintScreen(
    state: AppData,
    onCreateJob: (String, JobKind, Int, Boolean, Boolean, String) -> Unit,
    onAddHistory: (String, String) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var address by remember { mutableStateOf("https://www.example.com") }
    var title by remember { mutableStateOf("Web print") }
    var progress by remember { mutableIntStateOf(0) }
    var showHistory by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        return when {
            trimmed.isBlank() -> "https://www.example.com"
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> "https://$trimmed"
        }
    }

    fun load(raw: String) {
        val url = normalizeUrl(raw)
        address = url
        webView?.loadUrl(url)
    }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    Scaffold(
        topBar = {
            LexTopBar(
                title = "Web print",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Address") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(onGo = { load(address) })
                )
                IconButton(onClick = {
                    val current = webView ?: return@IconButton
                    if (current.canGoBack()) current.goBack() else current.reload()
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
                Button(onClick = { load(address) }) { Text("Go") }
            }

            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                progress = 1
                                address = url ?: address
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                progress = 100
                                val pageTitle = view?.title?.ifBlank { null } ?: url ?: address
                                title = pageTitle
                                if (!url.isNullOrBlank()) onAddHistory(url, pageTitle)
                            }
                        }
                        webView = this
                        loadUrl(address)
                    }
                },
                update = { view ->
                    webView = view
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val view = webView
                        if (view == null) {
                            Toast.makeText(context, "Nothing to print yet.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val device = state.defaultDevice
                        if (device == null) {
                            Toast.makeText(context, "Add a device first.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        PrintController.printWebView(
                            context,
                            view,
                            title.ifBlank { "Web page" },
                            state.printSettings
                        ).onSuccess {
                            onCreateJob(
                                title.ifBlank { "Web page" },
                                JobKind.WEB,
                                1,
                                state.printSettings.color,
                                state.printSettings.duplex,
                                "Sent to ${device.name}"
                            )
                        }.onFailure {
                            Toast.makeText(
                                context,
                                it.message ?: "Could not print this page.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null)
                    Text("  Print page")
                }
                TextButton(onClick = { showHistory = true }) { Text("History") }
            }
        }
    }

    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            title = { Text("Web history") },
            text = {
                Column {
                    if (state.webHistory.isEmpty()) {
                        Text("No pages visited yet.")
                    } else {
                        state.webHistory.take(10).forEach { entry ->
                            TextButton(
                                onClick = {
                                    load(entry.url)
                                    showHistory = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        entry.title.ifBlank { entry.url },
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        entry.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                                            .format(Date(entry.visitedAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistory = false }) { Text("Close") }
            },
            dismissButton = {
                if (state.webHistory.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onClearHistory()
                            showHistory = false
                        }
                    ) { Text("Clear") }
                }
            }
        )
    }
}
