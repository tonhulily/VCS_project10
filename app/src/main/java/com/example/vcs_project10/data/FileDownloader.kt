package com.example.vcs_project10.data

import android.content.Context
import android.util.Log
import com.example.vcs_project10.data.model.DownloadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class FileDownloader(
    private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    suspend fun downloadFile(
        item: DownloadItem,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(item.url)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .addHeader("Accept", "application/octet-stream, */*")
            .addHeader("Connection", "keep-alive")
            .addHeader("Referer", item.url)
            .build()

        try {
            client.newCall(request).execute().use { response ->

                if (!response.isSuccessful) {
                    Log.e("DOWNLOAD", "HTTP ${response.code}")
                    return@withContext
                }

                val body = response.body ?: return@withContext
                val totalBytes = body.contentLength().takeIf { it > 0 } ?: -1L

                val file = File(context.getExternalFilesDir(null), item.fileName)

                body.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->

                        val buffer = ByteArray(8192)
                        var downloadedBytes = 0L

                        while (true) {
                            ensureActive()

                            val read = inputStream.read(buffer)
                            if (read == -1) break

                            outputStream.write(buffer, 0, read)
                            downloadedBytes += read

                            val progress = if (totalBytes > 0) {
                                downloadedBytes.toFloat() / totalBytes
                            } else 0f

                            onProgress(progress)
                        }
                    }
                }

                Log.d("DOWNLOAD", "DONE")
            }
            Log.d("DOWNLOAD", "DONE")
        } catch (e: Exception) {
            Log.e("DOWNLOAD", "Download failed", e)
            throw e
        }
    }
}