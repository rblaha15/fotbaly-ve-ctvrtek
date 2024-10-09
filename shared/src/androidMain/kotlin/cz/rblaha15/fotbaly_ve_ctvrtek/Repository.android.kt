package cz.rblaha15.fotbaly_ve_ctvrtek

import android.app.NotificationManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.until


fun Context.createRepository() = Repository(
    settings = SharedPreferencesSettings.Factory(context = this).create("fotbaly_ve_ctvrtek"),
    notificationService = object : NotificationService {
        val workManager = WorkManager.getInstance(this@createRepository)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        override fun scheduleNotification(scheduleTime: Instant, day: NotificationDay) {
            val constraints = Constraints.Builder()
                .setTriggerContentMaxDelay(java.time.Duration.ZERO)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf("day" to day.name))
                .setInitialDelay(
                    Clock.System.now().until(scheduleTime, DateTimeUnit.MILLISECOND),
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueue(workRequest)
        }

        override fun cancelNotification() {
            workManager.cancelAllWork()
            notificationManager.cancelAll()
        }
    },
    firebaseDataSource = object : FirebaseDataSource {

        private val database = Firebase.database("https://fotbaly-ve-ctvrtek-default-rtdb.europe-west1.firebasedatabase.app/")
        private val answersReference = database.getReference("fotbaly_ve_ctvrtek")

        override suspend fun saveAnswer(person: Person, answer: AnswerState?) {
            if (answer == null)
                answersReference.child(person).removeValue().await()
            else
                answersReference.child(person).setValue(answer).await()
        }

        override val answers = answersReference.asFlow().map { snapshot ->
            snapshot.getValue<Map<Person, AnswerState>?>() ?: emptyMap()
        }

        override suspend fun clearAllAnswers() {
            answersReference.removeValue().await()
        }
    }
)

fun Query.asFlow(): Flow<DataSnapshot> = callbackFlow {
    val listener = addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            trySend(snapshot)
        }

        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    })
    awaitClose {
        removeEventListener(listener)
    }
}