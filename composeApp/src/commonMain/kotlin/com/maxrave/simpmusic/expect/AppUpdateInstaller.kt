package com.maxrave.simpmusic.expect

sealed interface DownloadUpdateState {
    data object Idle : DownloadUpdateState
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadUpdateState
    data class ReadyToInstall(val filePath: String) : DownloadUpdateState
    data class Error(val message: String) : DownloadUpdateState
}

expect fun downloadAndInstallUpdate(
    downloadUrl: String,
    versionName: String,
    onProgress: (DownloadUpdateState) -> Unit,
)

expect fun installDownloadedApk(filePath: String)

expect fun exitApplication()
