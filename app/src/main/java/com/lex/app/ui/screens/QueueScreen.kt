package com.lex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.JobStatus
import com.lex.app.ui.components.EmptyState
import com.lex.app.ui.components.JobCard
import com.lex.app.ui.components.LexTopBar
import com.lex.app.ui.components.SectionHeader

@Composable
fun QueueScreen(
    state: AppData,
    onBack: (() -> Unit)? = null,
    onReleaseAll: () -> Unit,
    onRelease: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearFinished: () -> Unit,
    onOpenDevices: () -> Unit
) {
    val pending = state.jobs.count {
        it.status == JobStatus.QUEUED || it.status == JobStatus.HELD
    }
    val completed = state.jobs.count { it.status == JobStatus.COMPLETED }
    val failed = state.jobs.count { it.status == JobStatus.FAILED }

    Scaffold(
        topBar = { LexTopBar(title = "Filas", onBack = onBack) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "  Fila de trabalhos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            "$pending pendentes · $completed concluídos · $failed com falha",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        androidx.compose.foundation.layout.Spacer(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onReleaseAll,
                                enabled = pending > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Filled.Print, contentDescription = null)
                                Text("  Liberar tudo")
                            }
                            OutlinedButton(
                                onClick = onClearFinished,
                                enabled = completed + failed > 0,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer)
                            ) { Text("Limpar concluídos") }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Trabalhos (${state.jobs.size})",
                    actionLabel = if (state.devices.isEmpty()) "Adicionar dispositivo" else null,
                    onAction = if (state.devices.isEmpty()) onOpenDevices else null
                )
            }

            if (state.jobs.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.History,
                        title = "A fila está vazia",
                        message = "Imprima, copie ou digitalize um documento e o trabalho aparecerá aqui."
                    )
                }
            } else {
                items(state.jobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        onRelease = { onRelease(job.id) },
                        onDelete = { onDelete(job.id) }
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Sobre a liberação de trabalhos",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Em uma implantação real de impressão segura, os trabalhos ficam retidos até você se autenticar na impressora. No LEX, Liberar simula esse fluxo localmente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowSpacer() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 4.dp))
}
