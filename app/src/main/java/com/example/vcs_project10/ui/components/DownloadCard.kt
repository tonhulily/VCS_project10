package com.example.vcs_project10.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vcs_project10.data.model.DownloadItem

@Composable
fun DownloadCard(
    item: DownloadItem,
    onStart: () -> Unit,
    onPause: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF4F46E5),
                            Color(0xFF7C3AED)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = item.fileName,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${(item.progress * 100).toInt()}%",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (item.isDownloading) {
                        onPause()
                    } else {
                        onStart()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Icon(
                    imageVector =
                        if (item.isDownloading) Icons.Default.Star
                        else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =
                        if (item.isDownloading) "Pause"
                        else "Download",
                    color = Color(0xFF4F46E5)
                )
            }
        }
    }
}