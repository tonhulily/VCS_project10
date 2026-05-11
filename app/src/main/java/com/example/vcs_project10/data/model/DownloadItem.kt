package com.example.vcs_project10.data.model

data class DownloadItem(
    val id: Int,
    val fileName: String,
    val url: String,
    val progress: Float = 0f,
    val isDownloading: Boolean = false,
    val isPaused: Boolean = false,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L
)