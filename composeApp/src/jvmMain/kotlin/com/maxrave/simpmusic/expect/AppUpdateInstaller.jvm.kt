package com.maxrave.simpmusic.expect

import kotlin.system.exitProcess

actual fun downloadAndInstallUpdate(
    downloadUrl: String,
    versionName: String,
    onProgress: (DownloadUpdateState) -> Unit,
) {
    openUrl(downloadUrl)
}

actual fun installDownloadedApk(filePath: String) {
    // No-op on JVM/Desktop
}

actual fun exitApplication() {
    exitProcess(0)
}
