package cz.rblaha15.fotbaly_ve_ctvrtek

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val repository = createRepository(context)

        scope.launch {
            val answer = AnswerState.valueOf(intent.getStringExtra("answer")!!)

            repository.saveAnswer(answer)

            repository.dismissNotification()
        }
    }
}