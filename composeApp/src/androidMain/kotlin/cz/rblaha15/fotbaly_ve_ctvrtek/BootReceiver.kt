package cz.rblaha15.fotbaly_ve_ctvrtek

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println(intent.action)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repo = createRepository(context)
            if (repo.alarmsEnabled() && repo.areNotificationsEnabled.value)
                repo.scheduleNearestNotification()
        }
    }
}