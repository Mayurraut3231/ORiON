package com.maxrave.simpmusic.expect

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.maxrave.logger.Logger
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
            // Clean up old update APKs in cache
            updateDir.listFiles()?.forEach { it.delete() }

            val cleanVersion = versionName.trim().removePrefix("v")
            val targetFile = File(updateDir, "ORiON-$cleanVersion.apk")

            withContext(Dispatchers.Main) {
                onProgress(DownloadUpdateState.Downloading(0f, 0L, 0L))
            }

            var currentUrl = downloadUrl
            var connection: HttpURLConnection
            var redirects = 0
            val maxRedirects = 8

            // Follow HTTP 301/302 redirects (GitHub Releases redirect to AWS S3)
            while (true) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "ORiON-Android-Updater")
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
                        throw Exception("Too many redirects while downloading update")
                    }
                    continue
                }
                break
            }

            if (connection.responseCode !in 200..299) {
                throw Exception("Server returned HTTP error ${connection.responseCode}")
            }

            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var read: Int
                    var lastUpdate = 0L

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 100 || (totalBytes > 0 && downloadedBytes == totalBytes)) {
                            lastUpdate = now
                            val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                            withContext(Dispatchers.Main) {
                                onProgress(DownloadUpdateState.Downloading(progress, downloadedBytes, totalBytes))
                            }
                        }
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(DownloadUpdateState.ReadyToInstall(targetFile.absolutePath))
                installDownloadedApk(targetFile.absolutePath)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Update download failed: ${e.message}")
            withContext(Dispatchers.Main) {
                onProgress(DownloadUpdateState.Error(e.message ?: "Failed to download update"))
            }
        }
    }
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
