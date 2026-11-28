package raut.mayur.orion.service.engagement

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MusicEngagementScheduler {
    const val WORK_TAG = "Music Engagement Worker"

    /**
     * Schedules periodic background recommendations (every 3 hours)
     * so that users receive intelligent music discoveries, mashup alerts,
     * and time-of-day mixes even when the app is completely closed.
     */
    fun schedule(context: Context) {
        val periodicRequest =
            PeriodicWorkRequestBuilder<MusicEngagementWorker>(
                3L,
                TimeUnit.HOURS,
            )
                .addTag(WORK_TAG)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest,
        )
    }

    /**
     * Triggers an immediate one-time recommendation check.
     */
    fun triggerImmediateCheck(context: Context) {
        val oneTimeRequest =
            OneTimeWorkRequestBuilder<MusicEngagementWorker>()
                .build()

        WorkManager.getInstance(context).enqueue(oneTimeRequest)
    }

    /**
     * Cancels the periodic background worker.
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
    }
}
