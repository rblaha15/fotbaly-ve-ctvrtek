package cz.rblaha15.fotbaly_ve_ctvrtek

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val day = NotificationDay.valueOf(intent.getStringExtra("day")!!)

        val repository = createRepository(context)
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val answer = repository.answers.first()[repository.getName()]

            if (answer == null || answer == AnswerState.Maybe)
                context.launchNotification(day)
        }

        repository.scheduleNotification(
            if (day == NotificationDay.Wednesday) NotificationDay.Tuesday
            else NotificationDay.Wednesday
        )
    }

    private fun Context.launchNotification(day: NotificationDay) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = getNotificationChannel()
        notificationManager.createNotificationChannel(channel)

        val notification = getNotification(channel, day == NotificationDay.Tuesday)
        notificationManager?.notify(1, notification)
    }

    private fun getNotificationChannel() = NotificationChannel(
        /* id = */ "fotbal",
        /* name = */ "Čutání u Jelena",
        /* importance = */ NotificationManager.IMPORTANCE_HIGH,
    )

    private fun Context.getNotification(channel: NotificationChannel, includeMaybe: Boolean) = Notification.Builder(
        /* context = */ this,
        /* channelId = */ channel.id,
    )
        .setContentTitle("Přijdeš na čtvrteční fotbal?")
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.icon)
        .setChannelId(channel.id)
        .setContentIntent(PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ))
        .let {
            val answers =
                if (includeMaybe) listOf(AnswerState.Yes, AnswerState.No, AnswerState.Maybe)
                else listOf(AnswerState.Yes, AnswerState.No)
            answers.fold(it) { builder, answer ->
                builder.addAction(
                    Notification.Action.Builder(
                        /* icon = */ null,
                        /* title = */ when (answer) {
                            AnswerState.Yes -> "Ano"
                            AnswerState.No -> "Ne"
                            AnswerState.Maybe -> "Nevím"
                        },
                        /* intent = */ PendingIntent.getBroadcast(
                            /* context = */ applicationContext,
                            /* requestCode = */ answer.ordinal,
                            /* intent = */ Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                                putExtra("answer", answer.name)
                            },
                            /* flags = */ PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    ).build()
                )
            }
        }
        .build()
}