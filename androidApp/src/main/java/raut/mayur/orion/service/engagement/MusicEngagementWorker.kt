package raut.mayur.orion.service.engagement

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import raut.mayur.domain.data.entities.NotificationEntity
import raut.mayur.domain.extension.now
import raut.mayur.domain.repository.CommonRepository
import raut.mayur.domain.repository.SongRepository
import raut.mayur.logger.Logger
import raut.mayur.orion.service.test.notification.NotificationHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Intelligent Music Engagement Worker
 *
 * Automatically sends music discovery, trending mashup, and contextual
 * recommendations (like Spotify, YouTube Music, and Apple Music) periodically
 * even when the app is completely closed.
 */
class MusicEngagementWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val commonRepository: CommonRepository by inject()
    private val songRepository: SongRepository by inject()

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                val prefs = applicationContext.getSharedPreferences("orion_engagement_prefs", Context.MODE_PRIVATE)
                val lastNotificationTime = prefs.getLong(KEY_LAST_NOTIFICATION_TIME, 0L)
                val now = System.currentTimeMillis()

                // Spacing interval: minimum 4 hours between notifications so user isn't spammed
                if (now - lastNotificationTime < MIN_INTERVAL_MS) {
                    Logger.d(TAG, "Notification skipped: sent recently within 4 hours")
                    return@withContext Result.success()
                }

                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

                val candidates = mutableListOf<CandidateNotification>()

                // 1. Personalized Song Recall (Spotify "Your Heavy Rotation")
                runCatching {
                    val songs = songRepository.getAllSongs(limit = 30).firstOrNull().orEmpty()
                    if (songs.isNotEmpty()) {
                        val pick = songs.random()
                        val artist = pick.artistName?.joinToString(", ") ?: "Artist"
                        candidates.add(
                            CandidateNotification(
                                id = "recall_${pick.videoId}_$dateStr",
                                title = "Time for Music",
                                message = "Continue listening to \"${pick.title}\" by $artist on ORiON.",
                                url = "simpmusic://watch?v=${pick.videoId}",
                                imageUrl = pick.thumbnails,
                            ),
                        )
                        candidates.add(
                            CandidateNotification(
                                id = "favorites_${pick.videoId}_$dateStr",
                                title = "Back in the Groove",
                                message = "\"${pick.title}\" is waiting for you in your library. Tap to play!",
                                url = "simpmusic://watch?v=${pick.videoId}",
                                imageUrl = pick.thumbnails,
                            ),
                        )
                    }
                }

                // 2. Trending Mashups & Viral Releases (Requested by User)
                candidates.add(
                    CandidateNotification(
                        id = "mashup_trend_1_$dateStr",
                        title = "🔥 Trending Mashups on ORiON",
                        message = "Have you heard today's viral mashup release? Tap to vibe out!",
                        url = "simpmusic://search",
                    ),
                )
                candidates.add(
                    CandidateNotification(
                        id = "mashup_trend_2_$dateStr",
                        title = "🎶 Fresh Mashups & Remixes",
                        message = "New mashups are heating up the charts. Explore what everyone is listening to!",
                        url = "simpmusic://search",
                    ),
                )
                candidates.add(
                    CandidateNotification(
                        id = "mashup_trend_3_$dateStr",
                        title = "⚡ Must-Listen Beat Drops",
                        message = "Looking for fresh energy? Discover trending remixes and mashups now on ORiON.",
                        url = "simpmusic://search",
                    ),
                )

                // 3. Time-of-Day Contextual Recommendations
                when (hour) {
                    in 6..11 -> {
                        // Morning
                        candidates.add(
                            CandidateNotification(
                                id = "morning_acoustic_$dateStr",
                                title = "☀️ Morning Acoustic & Chill",
                                message = "Start your day with refreshing melodies and gentle morning acoustic tracks.",
                                url = "simpmusic://home",
                            ),
                        )
                        candidates.add(
                            CandidateNotification(
                                id = "morning_coffee_$dateStr",
                                title = "☕ Coffee & Smooth Beats",
                                message = "The perfect morning soundtrack is ready. Wake up with ORiON!",
                                url = "simpmusic://home",
                            ),
                        )
                    }
                    in 12..16 -> {
                        // Afternoon
                        candidates.add(
                            CandidateNotification(
                                id = "afternoon_focus_$dateStr",
                                title = "⚡ Afternoon Focus & Flow",
                                message = "Power through your workday with upbeat lo-fi and deep focus playlists.",
                                url = "simpmusic://home",
                            ),
                        )
                        candidates.add(
                            CandidateNotification(
                                id = "afternoon_energy_$dateStr",
                                title = "🎧 Midday Energy Boost",
                                message = "Need a rhythm reset? Tune in to high-energy hits and mixes on ORiON.",
                                url = "simpmusic://home",
                            ),
                        )
                    }
                    in 17..20 -> {
                        // Evening
                        candidates.add(
                            CandidateNotification(
                                id = "evening_sunset_$dateStr",
                                title = "🌆 Sunset Vibes & Lounge",
                                message = "Wind down this evening with relaxing sunset rhythms and smooth vocals.",
                                url = "simpmusic://home",
                            ),
                        )
                        candidates.add(
                            CandidateNotification(
                                id = "evening_unwind_$dateStr",
                                title = "✨ Unwind with ORiON",
                                message = "Take a break after a busy day. Your favorite evening soundtrack awaits.",
                                url = "simpmusic://home",
                            ),
                        )
                    }
                    else -> {
                        // Night
                        candidates.add(
                            CandidateNotification(
                                id = "night_chill_$dateStr",
                                title = "🌙 Late Night Melodies",
                                message = "Relax and drift off with peaceful, ambient late-night tunes on ORiON.",
                                url = "simpmusic://home",
                            ),
                        )
                        candidates.add(
                            CandidateNotification(
                                id = "night_calm_$dateStr",
                                title = "🌌 Midnight Soundscapes",
                                message = "Calm your mind with mellow beats and soothing nighttime melodies.",
                                url = "simpmusic://home",
                            ),
                        )
                    }
                }

                // 4. Weekend / Activity Vibes
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    candidates.add(
                        CandidateNotification(
                            id = "weekend_party_$dateStr",
                            title = "🎉 Weekend Party Anthems",
                            message = "It's the weekend! Crank up the volume with hot club mixes and party chartbusters.",
                            url = "simpmusic://home",
                        ),
                    )
                } else {
                    candidates.add(
                        CandidateNotification(
                            id = "workout_energy_$dateStr",
                            title = "💪 Workout Motivation Beats",
                            message = "Crush your workout today with heart-pumping gym beats and high BPM anthems!",
                            url = "simpmusic://home",
                        ),
                    )
                    candidates.add(
                        CandidateNotification(
                            id = "romantic_hits_$dateStr",
                            title = "❤️ Feel-Good Romantic Hits",
                            message = "Fall in love with timeless melodies and soulful acoustic songs on ORiON.",
                            url = "simpmusic://home",
                        ),
                    )
                }

                // Shuffle candidates and pick one that hasn't been sent yet
                candidates.shuffle(Random(System.currentTimeMillis()))
                var selected: CandidateNotification? = null
                for (cand in candidates) {
                    val dedupKey = "engagement:${cand.id}"
                    if (!commonRepository.isNotificationExists(dedupKey)) {
                        selected = cand
                        break
                    }
                }

                if (selected != null) {
                    val dedupKey = "engagement:${selected.id}"
                    NotificationHandler.createMusicNotification(
                        applicationContext,
                        title = selected.title,
                        text = selected.message,
                        url = selected.url,
                        imageUrl = selected.imageUrl,
                    )

                    commonRepository.insertNotification(
                        NotificationEntity(
                            channelId = "orion_music_discovery",
                            name = selected.title,
                            description = selected.message,
                            link = dedupKey,
                            type = NotificationEntity.TYPE_BLOG,
                            time = now(),
                        ),
                    )

                    prefs.edit().putLong(KEY_LAST_NOTIFICATION_TIME, now).apply()
                    Logger.d(TAG, "Music recommendation notification pushed: ${selected.title}")
                }

                Result.success()
            } catch (e: Exception) {
                Logger.e(TAG, "Error running music engagement worker: ${e.message}")
                Result.retry()
            }
        }

    private data class CandidateNotification(
        val id: String,
        val title: String,
        val message: String,
        val url: String,
        val imageUrl: String? = null,
    )

    companion object {
        private const val TAG = "MusicEngagementWorker"
        private const val KEY_LAST_NOTIFICATION_TIME = "last_notification_time"
        private const val MIN_INTERVAL_MS = 4 * 60 * 60 * 1000L // 4 hours
    }
}
