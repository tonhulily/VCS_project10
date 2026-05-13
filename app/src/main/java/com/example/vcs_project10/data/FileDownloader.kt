package com.example.vcs_project10.data

import android.content.Context
import android.util.Log
import com.example.vcs_project10.data.model.DownloadItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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
        val file = File(
            context.getExternalFilesDir(null),
            item.fileName
        )

        if (item.progress >= 1f) {
            file.delete()
        }
        var downloadedBytes =
            if (file.exists()) {
                file.length()
            } else {
                0L
            }

        try {
            var response = createRequest(
                url = item.url,
                downloadedBytes = downloadedBytes
            )
            if (response.code == 416) {
                Log.d(
                    "DOWNLOAD",
                    "416 -> delete old file"
                )
                response.close()
                file.delete()
                downloadedBytes = 0L
                response = createRequest(
                    url = item.url,
                    downloadedBytes = 0L
                )
            }
            if (
                downloadedBytes > 0 &&
                response.code == 200
            ) {
                Log.d(
                    "DOWNLOAD",
                    "Server does not support resume"
                )
                response.close()
                file.delete()
                downloadedBytes = 0L
                response = createRequest(
                    url = item.url,
                    downloadedBytes = 0L
                )
            }
            if (!response.isSuccessful) {
                Log.e(
                    "DOWNLOAD",
                    "HTTP ${response.code}"
                )
                response.close()
                return@withContext
            }
            val body = response.body
            if (body == null) {
                response.close()
                return@withContext
            }
            val totalBytes =
                getTotalFileSize(
                    response = response,
                    downloadedBytes = downloadedBytes,
                    bodyLength = body.contentLength()
                )
            var currentBytes = downloadedBytes
            body.byteStream().use { inputStream ->
                FileOutputStream(
                    file,
                    downloadedBytes > 0
                ).use { outputStream ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        ensureActive()
                        val read = inputStream.read(buffer)
                        if (read == -1) break
                        outputStream.write(
                            buffer,
                            0,
                            read
                        )
                        currentBytes += read
                        val progress =
                            if (totalBytes > 0) {
                                currentBytes.toFloat() /
                                        totalBytes.toFloat()
                            } else {
                                0f
                            }
                        onProgress(progress)
                    }
                    outputStream.flush()
                }
            }
            response.close()
            onProgress(1f)
            Log.d("DOWNLOAD", "DONE")
        } catch (e: CancellationException) {
            Log.d(
                "DOWNLOAD",
                "Download paused"
            )
            throw e

        } catch (e: Exception) {
            Log.e(
                "DOWNLOAD",
                "Download failed",
                e
            )
            throw e
        }
    }
    private fun createRequest(
        url: String,
        downloadedBytes: Long
    ): Response {
        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            )
            .addHeader(
                "Accept",
                "application/octet-stream, */*"
            )
            .addHeader(
                "Connection",
                "keep-alive"
            )
        if (downloadedBytes > 0) {
            requestBuilder.addHeader(
                "Range",
                "bytes=$downloadedBytes-"
            )
        }
        return client.newCall(
            requestBuilder.build()
        ).execute()
    }
    private fun getTotalFileSize(
        response: Response,
        downloadedBytes: Long,
        bodyLength: Long
    ): Long {
        val contentRange =
            response.header("Content-Range")
        if (
            contentRange != null &&
            contentRange.contains("/")
        ) {
            val totalSize =
                contentRange
                    .substringAfter("/")
                    .toLongOrNull()

            if (totalSize != null) {
                return totalSize
            }
        }
        return if (downloadedBytes > 0) {
            downloadedBytes + bodyLength
        } else {
            bodyLength
        }
    }
}