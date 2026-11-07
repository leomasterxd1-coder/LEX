package com.lex.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    onFinish: (addDemoDevice: Boolean, dataCollection: Boolean) -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var dataCollection by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    "LEX",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Print, scan and manage documents from your phone.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            when (step) {
                0 -> IntroStep()
                1 -> FeaturesStep()
                2 -> ConsentStep(
                    dataCollection = dataCollection,
                    onDataCollection = { dataCollection = it }
                )
                else -> AddDeviceStep()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onFinish(false, dataCollection) }) {
                Text("Skip")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == step) 10.dp else 7.dp)
                            .background(
                                if (index == step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                CircleShape
                            )
                    )
                }
                Spacer(Modifier.padding(horizontal = 8.dp))
                if (step < 3) {
                    Button(onClick = { step++ }) { Text("Next") }
                } else {
                    Button(onClick = { onFinish(true, dataCollection) }) { Text("Get started") }
                }
            }
        }
    }
}

@Composable
private fun IntroStep() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Print,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Welcome to LEX",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "A clean, fast Compose experience for printing and scanning. Add a device or use the demo printer to explore every feature.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeaturesStep() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Everything in one place",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        FeatureLine(Icons.Filled.Print, "Print from files, photos and the web")
        FeatureLine(Icons.Filled.Scanner, "Scan with the camera, photos or a simulated scanner")
        FeatureLine(Icons.Filled.Wifi, "Discover compatible printers on your network")
        FeatureLine(Icons.Filled.Cloud, "Queue jobs and manage print release")
        Spacer(Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                "LEX is an independent clean-room project. It is not affiliated with, endorsed by or sponsored by Lexmark.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ConsentStep(dataCollection: Boolean, onDataCollection: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.PrivacyTip,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.padding(horizontal = 6.dp))
            Text(
                "Your privacy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "LEX stores devices, settings and job history only on this device. No account is required.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Share anonymous usage data", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Optional diagnostics help improve LEX. Off by default.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = dataCollection, onCheckedChange = onDataCollection)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "You can change this later in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AddDeviceStep() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Add a device",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "You can add a demo printer now and discover real network printers later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LEX Demo Printer", fontWeight = FontWeight.SemiBold)
                Text(
                    "192.168.1.100 · Printer and Scanner",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { }) {
                    Text("It will be added when you tap Get started")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Prefer to start without a device? Tap Skip and add one from the Devices tab.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureLine(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.padding(horizontal = 8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
