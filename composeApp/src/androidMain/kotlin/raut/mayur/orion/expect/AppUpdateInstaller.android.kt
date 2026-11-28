package raut.mayur.orion.expect

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import raut.mayur.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.exitProcess

private const val TAG = "AppUpdateInstaller"

actual fun downloadAndInstallUpdate(
    downloadUrl: String,
    versionName: String,
    onProgress: (DownloadUpdateState) -> Unit,
) {
    val activity: AppCompatActivity = try {
        getKoin().get()
    } catch (e: Throwable) {
        Logger.e(TAG, "Failed to get activity: ${e.message}")
        onProgress(DownloadUpdateState.Error("Unable to access application context"))
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val updateDir = File(activity.cacheDir, "updates")
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }

            val cleanVersion = versionName.trim().removePrefix("v")
            val targetFile = File(updateDir, "ORiON-$cleanVersion.apk")
            val tempFile = File(updateDir, "ORiON-$cleanVersion.apk.part")

            // Candidate URLs: Direct GitHub + Accelerated CDN Mirrors
            val candidates = mutableListOf<String>()
            candidates.add(downloadUrl)
            if (downloadUrl.startsWith("https://github.com/")) {
                candidates.add("https://ghproxy.net/$downloadUrl")
                candidates.add("https://gh-proxy.com/$downloadUrl")
            }

            var downloadSuccess = false
            var lastError = "Download failed"

            withContext(Dispatchers.Main) {
                onProgress(DownloadUpdateState.Downloading(0f, 0L, 0L))
            }

            for (candidateUrl in candidates) {
                if (downloadSuccess) break

                var attempts = 0
                val maxAttempts = 3

                while (attempts < maxAttempts && !downloadSuccess) {
                    attempts++
                    try {
                        val existingBytes = if (tempFile.exists()) tempFile.length() else 0L
                        val downloaded = downloadChunked(
                            sourceUrl = candidateUrl,
                            tempFile = tempFile,
                            existingBytes = existingBytes,
                            onProgressUpdate = { progress, current, total ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    onProgress(DownloadUpdateState.Downloading(progress, current, total))
                                }
                            },
                        )

                        if (downloaded) {
                            if (tempFile.exists() && tempFile.length() > 1024 * 1024) {
                                if (targetFile.exists()) targetFile.delete()
                                tempFile.renameTo(targetFile)
                                downloadSuccess = true
                                break
                            }
                        }
                    } catch (e: Throwable) {
                        Logger.w(TAG, "Attempt $attempts failed on $candidateUrl: ${e.message}")
                        lastError = e.message ?: "Connection reset by peer"
                        // Brief pause before resuming
                        kotlinx.coroutines.delay(1000)
                    }
                }
            }

            if (downloadSuccess && targetFile.exists()) {
                withContext(Dispatchers.Main) {
                    onProgress(DownloadUpdateState.ReadyToInstall(targetFile.absolutePath))
                    installDownloadedApk(targetFile.absolutePath)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onProgress(DownloadUpdateState.Error("Network throttled by ISP: $lastError. Please use 'Download via Browser'."))
                }
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Critical updater failure: ${e.message}")
            withContext(Dispatchers.Main) {
                onProgress(DownloadUpdateState.Error(e.message ?: "Failed to download update"))
            }
        }
    }
}

/**
 * Downloads with automatic HTTP Range resuming, custom browser headers, and redirect following.
 */
private fun downloadChunked(
    sourceUrl: String,
    tempFile: File,
    existingBytes: Long,
    onProgressUpdate: (Float, Long, Long) -> Unit,
): Boolean {
    var currentUrl = sourceUrl
    var connection: HttpURLConnection? = null
    var redirects = 0
    val maxRedirects = 8

    while (true) {
        val url = URL(currentUrl)
        connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 30000
        connection.readTimeout = 45000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
        connection.setRequestProperty("Accept", "*/*")
        connection.setRequestProperty("Connection", "keep-alive")

        if (existingBytes > 0 && redirects == 0) {
            connection.setRequestProperty("Range", "bytes=$existingBytes-")
        }

        connection.connect()

        val status = connection.responseCode
        if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
            status == HttpURLConnection.HTTP_MOVED_PERM ||
            status == HttpURLConnection.HTTP_SEE_OTHER ||
            status == 307 || status == 308
        ) {
            currentUrl = connection.getHeaderField("Location") ?: break
            connection.disconnect()
            redirects++
            if (redirects > maxRedirects) {
                throw Exception("Too many redirects")
            }
            continue
        }
        break
    }

    val conn = connection ?: throw Exception("Failed to open connection")
    val status = conn.responseCode

    val isResume = (status == 206)
    if (status != 200 && status != 206) {
        throw Exception("Server returned HTTP $status")
    }

    val contentLength = conn.contentLength.toLong()
    val totalBytes = if (isResume) existingBytes + contentLength else contentLength
    var downloadedBytes = if (isResume) existingBytes else 0L

    val appendMode = isResume
    if (!appendMode && tempFile.exists()) {
        tempFile.delete()
    }

    conn.inputStream.use { input ->
        FileOutputStream(tempFile, appendMode).use { output ->
            val buffer = ByteArray(128 * 1024) // 128KB buffer
            var read: Int
            var lastUpdate = 0L

            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                downloadedBytes += read

                val now = System.currentTimeMillis()
                if (now - lastUpdate > 120 || (totalBytes > 0 && downloadedBytes == totalBytes)) {
                    lastUpdate = now
                    val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                    onProgressUpdate(progress, downloadedBytes, totalBytes)
                }
            }
            output.flush()
        }
    }

    return tempFile.exists() && (totalBytes <= 0 || tempFile.length() >= totalBytes)
}

actual fun installDownloadedApk(filePath: String) {
    val activity: AppCompatActivity = try {
        getKoin().get()
    } catch (e: Throwable) {
        Logger.e(TAG, "Failed to get activity for install: ${e.message}")
        return
    }

    try {
        val apkFile = File(filePath)
        if (!apkFile.exists()) {
            Logger.e(TAG, "Apk file does not exist at $filePath")
            return
        }

        // On Android 8.0+ (Oreo), verify unknown app install permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.packageManager.canRequestPackageInstalls()) {
                val manageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${activity.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(manageIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.FileProvider",
            apkFile,
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(installIntent)
    } catch (e: Throwable) {
        Logger.e(TAG, "Failed to launch package installer: ${e.message}")
    }
}

actual fun exitApplication() {
    try {
        val activity: AppCompatActivity = getKoin().get()
        activity.finishAffinity()
    } catch (_: Throwable) {}
    try {
        android.os.Process.killProcess(android.os.Process.myPid())
    } catch (_: Throwable) {}
    exitProcess(0)
}
