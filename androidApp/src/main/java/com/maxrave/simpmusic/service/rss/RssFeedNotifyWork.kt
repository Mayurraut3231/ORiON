package com.maxrave.simpmusic.service.rss

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maxrave.domain.data.entities.NotificationEntity
import com.maxrave.domain.extension.now
import com.maxrave.domain.repository.CommonRepository
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.service.test.notification.NotificationHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Periodically scans the ORiON announcements feed (JSON) and pushes a local
 * notification for each announcement that is not already stored in the notification DB.
 *
 * This allows the developer to send notifications to all users remotely from GitHub
 * without pushing an app update.
 */
class RssFeedNotifyWork(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val commonRepository: CommonRepository by inject()

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                val content = fetchFeed(FEED_URL)
                if (content.isBlank()) return@withContext Result.success()

                val trimmed = content.trim()
                if (trimmed.startsWith("[")) {
                    val jsonArray = JSONArray(trimmed)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.optJSONObject(i) ?: continue
                        val id = item.optString("id", "").trim()
                        val title = item.optString("title", "ORiON Music").trim()
                        val message = item.optString("message", "").trim()
                        val url = item.optString("url", "https://github.com/Mayurraut3231/ORiON").trim()
                        val imageUrl = item.optString("image", "").trim()

                        if (id.isEmpty() || message.isEmpty()) continue

                        val dedupKey = "announcement:$id"
                        if (!commonRepository.isNotificationExists(dedupKey)) {
                            NotificationHandler.createBlogNotification(
                                applicationContext,
                                title = title,
                                text = message,
                                url = url,
                                imageUrl = if (imageUrl.isNotEmpty()) imageUrl else null,
                            )
                            commonRepository.insertNotification(
                                NotificationEntity(
                                    channelId = "orion_announcements",
                                    name = title,
                                    description = message,
                                    link = dedupKey,
                                    type = NotificationEntity.TYPE_BLOG,
                                    time = now(),
                                ),
                            )
                            Logger.d(TAG, "ORiON announcement pushed: $id - $title")
                        }
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Logger.e(TAG, "Error checking announcements: ${e.message}")
                Result.retry()
            }
        }

    private fun fetchFeed(urlStr: String): String {
        val connection =
            (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 15_000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "ORiON-Music")
            }
        return try {
            if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val TAG = "RssFeedNotifyWork"
        const val FEED_URL = "https://raw.githubusercontent.com/Mayurraut3231/ORiON/main/announcements.json"
    }
}

