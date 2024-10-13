package cz.rblaha15.fotbaly_ve_ctvrtek

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val day = inputData.getString("day")?.let { NotificationDay.valueOf(it) } ?: return Result.failure()


        val repository = applicationContext.createRepository()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val answer = repository.answers.first().toList()
                .find { it.first == repository.getName() }?.second

            if (answer != null && answer != AnswerState.Maybe) {
                repository.scheduleNewNotification(true)
            } else {
                val notification = getNotification(day)
                val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)

                val channel = NotificationChannel(
                    /* id = */ "fotbal",
                    /* name = */ "Čutání u Jelena",
                    /* importance = */ NotificationManager.IMPORTANCE_HIGH,
                )
                notificationManager.createNotificationChannel(channel)

                notificationManager?.notify(1, notification)
            }
        }
        return Result.success()
    }

    private fun getNotification(day: NotificationDay) = Notification.Builder(
        /* context = */ applicationContext,
        /* channelId = */ "fotbal"
    )
        .setContentTitle("Přijdeš na čtvrteční fotbal?")
//        .setContentText(day.name)
        .setStyle(Notification.BigTextStyle().bigText("Přijdeš na čtvrteční fotbal?")!!)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(com.google.android.gms.base.R.drawable.common_full_open_on_phone)
        .setChannelId("fotbal")
        .let {
            val answers = when (day) {
                NotificationDay.Tuesday -> listOf(AnswerState.Yes, AnswerState.No, AnswerState.Maybe)
                NotificationDay.Wednesday -> listOf(AnswerState.Yes, AnswerState.No)
            }
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