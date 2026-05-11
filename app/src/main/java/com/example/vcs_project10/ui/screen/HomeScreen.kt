package com.example.vcs_project10.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vcs_project10.ui.components.DownloadCard
import com.example.vcs_project10.DownloadViewModel

@Composable
fun HomeScreen() {
    val viewModel: DownloadViewModel = viewModel()
    val downloads by viewModel.downloads.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF5F7FF),
                        Color(0xFFE0E7FF)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Mini Download Manager",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF312E81)
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            items(downloads) { item ->
                DownloadCard(
                    item = item,
                    onStart = {
                        viewModel.startDownload(item)
                    },
                    onPause = {
                        viewModel.pauseDownload(item)
                    }
                )
            }
        }
    }
}