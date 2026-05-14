package com.example.vcs_project10.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vcs_project10.data.model.DownloadItem

@Composable
fun DownloadCard(
    item: DownloadItem,
    onStart: () -> Unit,
    onPause: () -> Unit
) {
    val actionColor: Color
    val actionText: String
    val actionIcon = when {
        item.isDownloading -> {
            actionColor = Color(0xFFF59E0B)
            actionText = "Pause"
            Icons.Default.PauseCircle
        }
        item.isPaused -> {
            actionColor = Color(0xFF06B6D4)
            actionText = "Resume"
            Icons.Default.PlayCircle
        }
        item.progress >= 1f -> {
            actionColor = Color(0xFF22C55E)
            actionText = "Download Again"
            Icons.Default.DownloadForOffline
        }
        else -> {
            actionColor = Color(0xFF3B82F6)
            actionText = "Download"
            Icons.Default.DownloadForOffline
        }
    }
    val statusText = when {
        item.progress >= 1f -> "Completed"
        item.isDownloading -> "Downloading..."
        item.isPaused -> "Paused"
        else -> "Ready"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            actionColor.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = actionColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "${(item.progress * 100).toInt()}%",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = actionColor,
                trackColor = Color(0xFFE2E8F0),
                gapSize = 0.dp,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = {
                    if (item.isDownloading) {
                        onPause()
                    } else {
                        onStart()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = actionColor,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = actionText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}