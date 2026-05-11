package com.example.vcs_project10.data

import android.content.Context
import com.example.vcs_project10.data.model.DownloadItem

class DownloadRepository(
    context: Context
) {
    private val downloader = FileDownloader(context)
    suspend fun download(
        item: DownloadItem,
        onProgress: (Float) -> Unit
    ) {
        downloader.downloadFile(
            item,
            onProgress
        )
    }
}