package raut.mayur.orion.service.rss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import raut.mayur.orion.service.engagement.MusicEngagementScheduler

class AnnouncementBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            AnnouncementScheduler.schedule(context)
            AnnouncementScheduler.triggerImmediateCheck(context)
            MusicEngagementScheduler.schedule(context)
        }
    }
}
