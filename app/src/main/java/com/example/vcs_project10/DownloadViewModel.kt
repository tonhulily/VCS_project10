package com.example.vcs_project10

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vcs_project10.data.DownloadRepository
import com.example.vcs_project10.data.model.DownloadItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class DownloadViewModel(
    application: Application
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "DOWNLOAD"
        private const val CHANNEL_ID = "download_channel"
    }
    private val repository = DownloadRepository(application)
    private val _downloads = MutableStateFlow(repository.getDownloads())
    val downloads: StateFlow<List<DownloadItem>> get() = _downloads
    private val jobs = mutableMapOf<Int, Job>()
    private val notificationManager =
        application.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
    private val lastNotificationTime = mutableMapOf<Int, Long>()
    private val lastNotificationProgress = mutableMapOf<Int, Int>()
    init {
        createNotificationChannel()
    }
    fun startDownload(item: DownloadItem) {
        if (jobs[item.id]?.isActive == true) {
            return
        }
        updateItem(item.id) {
            it.copy(
                isDownloading = true,
                isPaused = false
            )
        }
        showNotification(
            id = item.id,
            title =
                if (item.progress > 0f)
                    "Resume Download"
                else
                    "Download Started",
            message =
                if (item.progress > 0f)
                    "Resuming ${item.fileName}"
                else
                    "Downloading ${item.fileName}"
        )
        val job = viewModelScope.launch {
            try {
                repository.download(item) { progress ->
                    updateItem(item.id) {
                        it.copy(progress = progress)
                    }
                    val progressPercent = (progress * 100).toInt()
                    val currentTime = System.currentTimeMillis()
                    val lastTime = lastNotificationTime[item.id] ?: 0L
                    val lastProgress = lastNotificationProgress[item.id] ?: -1
                    val shouldUpdate = progressPercent != lastProgress && currentTime - lastTime >= 500

                    if (shouldUpdate) {
                        lastNotificationProgress[item.id] = progressPercent
                        lastNotificationTime[item.id] = currentTime

                        showProgressNotification(
                            id = item.id,
                            fileName = item.fileName,
                            progress = progressPercent
                        )
                    }
                }
                updateItem(item.id) {
                    it.copy(
                        progress = 1f,
                        isDownloading = false,
                        isPaused = false
                    )
                }
                showNotification(
                    id = item.id,
                    title = "Download Completed",
                    message = "${item.fileName} downloaded successfully"
                )
                Log.d(TAG, "Download completed")
            } catch (_: CancellationException) {
                Log.d(TAG, "Download paused")
                showNotification(
                    id = item.id,
                    title = "Download Paused",
                    message = "${item.fileName} paused"
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Download failed: ${e.message}",
                    e
                )
                showNotification(
                    id = item.id,
                    title = "Download Failed",
                    message = e.message ?: "Unknown error"
                )
            } finally {
                val currentItem =
                    _downloads.value.first {
                        it.id == item.id
                    }
                updateItem(item.id) {
                    it.copy(
                        isDownloading = false,
                        isPaused =
                            currentItem.progress < 1f
                    )
                }
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
        transform: (DownloadItem) -> DownloadItem
    ) {
        _downloads.value =
            _downloads.value.map { item ->

                if (item.id == id) {
                    transform(item)
                } else {
                    item
                }
            }
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Download progress notifications"
        }
        notificationManager.createNotificationChannel(channel)
    }
    private fun showNotification(
        id: Int,
        title: String,
        message: String
    ) {
        val notification =
            NotificationCompat.Builder(
                getApplication(),
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.stat_sys_download_done
                )
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(
                    NotificationCompat.PRIORITY_LOW
                )
                .setOnlyAlertOnce(true)
                .build()
        notificationManager.notify(
            id,
            notification
        )
    }
    private fun showProgressNotification(
        id: Int,
        fileName: String,
        progress: Int
    ) {
        val notification =
            NotificationCompat.Builder(
                getApplication(),
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.stat_sys_download
                )
                .setContentTitle(fileName)
                .setContentText(
                    "Downloading... $progress%"
                )
                .setOnlyAlertOnce(true)
                .setOngoing(progress < 100)
                .setProgress(
                    100,
                    progress,
                    false
                )
                .setPriority(
                    NotificationCompat.PRIORITY_LOW
                )
                .build()

        notificationManager.notify(
            id,
            notification
        )
    }
}