package com.example.vcs_project10

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vcs_project10.data.DownloadRepository
import com.example.vcs_project10.data.model.DownloadItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DownloadViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = DownloadRepository(application)
    private val _downloads = MutableStateFlow(sampleDownloads())
    val downloads: StateFlow<List<DownloadItem>> = _downloads
    private val jobs = mutableMapOf<Int, Job>()
    fun startDownload(item: DownloadItem) {
        if (jobs[item.id]?.isActive == true) return
        updateItem(item.id) { it.copy(isDownloading = true, isPaused = false) }
        val job = viewModelScope.launch {
            try {
                repository.download(item) { progress ->
                    updateItem(item.id) { it.copy(progress = progress) }
                }
            } catch (e: Exception) {
                Log.e("DOWNLOAD", "Lỗi tải file: ${e.message}")
            } finally {
                updateItem(item.id) { it.copy(isDownloading = false) }
                jobs.remove(item.id)
            }
        }
        jobs[item.id] = job
    }
    fun pauseDownload(item: DownloadItem) {
        jobs[item.id]?.cancel()
        updateItem(item.id) {
            it.copy(
                isDownloading = false,
                isPaused = true
            )
        }
    }
    private fun updateItem(
        id: Int,
        update: (DownloadItem) -> DownloadItem
    ) {
        _downloads.value = _downloads.value.map {
            if (it.id == id) update(it)
            else it
        }
    }
    private fun sampleDownloads(): List<DownloadItem> {
        return listOf(
            DownloadItem(1, "Test_10MB.zip", "http://ipv4.download.thinkbroadband.com/10MB.zip"),
            DownloadItem(2, "Chrome_Installer.exe", "https://dl.google.com/chrome/install/375.126/chrome_installer.exe"),
            DownloadItem(3, "Sample_Video.mp4", "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        )
    }
}