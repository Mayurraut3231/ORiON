package raut.mayur.orion.service.rss

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AnnouncementScheduler {
    const val WORK_TAG = "Blog RSS Worker"

    /**
     * Schedules periodic background polling (every 15 minutes, the minimum allowed by Android)
     * so that notifications for new releases, mashups, and announcements arrive even when the
     * app is closed.
     */
    fun schedule(context: Context) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val periodicRequest =
            PeriodicWorkRequestBuilder<RssFeedNotifyWork>(
                15L,
                TimeUnit.MINUTES,
            )
                .addTag(WORK_TAG)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest,
        )
    }

    /**
     * Triggers an immediate one-time background check for new releases.
     */
    fun triggerImmediateCheck(context: Context) {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val oneTimeRequest =
            OneTimeWorkRequestBuilder<RssFeedNotifyWork>()
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context).enqueue(oneTimeRequest)
    }

    /**
     * Cancels the background polling worker.
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
    }
}
