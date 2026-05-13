package com.example.vcs_project10.data

import com.example.vcs_project10.data.model.DownloadItem

object DownloadSource {
    fun getSampleDownloads(): List<DownloadItem> {
        return listOf(
            DownloadItem(
                id = 1,
                fileName = "Test_10MB.zip",
                url = "https://proof.ovh.net/files/10Mb.dat"
            ),
            DownloadItem(
                id = 2,
                fileName = "Sample_100MB.bin",
                url = "https://ash-speed.hetzner.com/100MB.bin"
            ),
            DownloadItem(
                id = 3,
                fileName = "BigBuckBunny.mp4",
                url = "https://download.samplelib.com/mp4/sample-20s.mp4"
            )
        )
    }
}