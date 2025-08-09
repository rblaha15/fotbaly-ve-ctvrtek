package cz.rblaha15.fotbaly_ve_ctvrtek

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
fun createRepository(context: Context) = Repository(
    settings = settings(context),
    notificationService = AndroidNotificationService(context),
    firebaseDataSource = AndroidFirebaseDataSource,
)

@OptIn(ExperimentalTime::class)
class AndroidNotificationService(private val context: Context) : NotificationService {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val baseIntent = Intent(context, NotificationReceiver::class.java)

    private fun Intent.toPendingIntent() =
        PendingIntent.getBroadcast(context, 1, this, PendingIntent.FLAG_MUTABLE + PendingIntent.FLAG_UPDATE_CURRENT)

    override fun scheduleNotification(scheduleTime: Instant, extras: Map<String, String>) {
        val intent = baseIntent
        extras.forEach { (key, value) ->
            intent.putExtra(key, value)
        }

        val pendingIntent = intent.toPendingIntent()
        alarmManager.setExactAndAllowWhileIdle(
            /* type = */ AlarmManager.RTC_WAKEUP,
            /* triggerAtMillis = */ scheduleTime.toEpochMilliseconds(),
            /* operation = */ pendingIntent,
        )
    }

    override fun cancelNotification() {
        alarmManager.cancel(baseIntent.toPendingIntent())
    }

    override fun dismissNotification() {
        notificationManager.cancelAll()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun alarmsEnabled() = alarmManager.canScheduleExactAlarms()
}