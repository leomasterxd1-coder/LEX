package com.lex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lex.app.data.AppData
import com.lex.app.data.JobStatus
import com.lex.app.ui.components.JobCard
import com.lex.app.ui.components.LexTopBar

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
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentJobs = state.jobs.filter {
        it.status == JobStatus.QUEUED ||
            it.status == JobStatus.HELD ||
            it.status == JobStatus.PROCESSING
    }
    val historyJobs = state.jobs.filter {
        it.status == JobStatus.COMPLETED || it.status == JobStatus.FAILED
    }

    Scaffold(
        topBar = { LexTopBar(title = "Status do trabalho", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "ATUAL",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "HISTÓRICO",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }

            val jobs = if (selectedTab == 0) currentJobs else historyJobs
            if (jobs.isEmpty()) {
                JobEmptyState(history = selectedTab == 1)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (selectedTab == 0) {
                                TextButton(onClick = onReleaseAll) { Text("Liberar tudo") }
                            } else {
                                TextButton(onClick = onClearFinished) { Text("Limpar histórico") }
                            }
                        }
                    }
                    items(jobs, key = { it.id }) { job ->
                        JobCard(
                            job = job,
                            onRelease = { onRelease(job.id) },
                            onDelete = { onDelete(job.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JobEmptyState(history: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.16f),
            modifier = Modifier.size(190.dp)
        )
        Text(
            text = if (history) "Nenhum trabalho no histórico." else "Nenhum trabalho atual.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
