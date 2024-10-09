package cz.rblaha15.fotbaly_ve_ctvrtek

import com.russhwolf.settings.StorageSettings
import firebase.database.DataSnapshot
import firebase.database.DatabaseReference
import firebase.database.child
import firebase.database.getDatabase
import firebase.database.onValue
import firebase.database.ref
import firebase.database.remove
import firebase.database.set
import firebase.initializeApp
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlin.js.Json


val firebaseConfig = mapOf(
    "apiKey" to "AIzaSyBnc1SKJcOEWcY484Ilc-29iskxJskhSm8",
    "authDomain" to "fotbaly-ve-ctvrtek.firebaseapp.com",
    "databaseURL" to "https://fotbaly-ve-ctvrtek-default-rtdb.europe-west1.firebasedatabase.app",
    "projectId" to "fotbaly-ve-ctvrtek",
    "storageBucket" to "fotbaly-ve-ctvrtek.appspot.com",
    "messagingSenderId" to "530653255530",
    "appId" to "1:530653255530:web:6b9a5e0c8da07437c5f713",
    "measurementId" to "G-MTLT9YNXJB",
)

fun createRepository() = Repository(
    settings = StorageSettings(),
    notificationService = object : NotificationService {
        override fun scheduleNotification(scheduleTime: Instant, day: NotificationDay) {}
        override fun cancelNotification() {}
    },
    firebaseDataSource = object : FirebaseDataSource {

        val app = initializeApp(firebaseConfig)
        val database = getDatabase(app, "https://fotbaly-ve-ctvrtek-default-rtdb.europe-west1.firebasedatabase.app")
        val reference = ref(database, "fotbaly_ve_ctvrtek")

        override val answers = reference.asFlow().map { snapshot ->
            snapshot.value<Json?>()
                ?.toMap()
                ?.mapValues { (_, value) -> AnswerState.valueOf(value) }
                ?: emptyMap()
        }

        override suspend fun saveAnswer(person: Person, answer: AnswerState?) {
            if (answer == null)
                remove(child(reference, person)).await()
            else
                set(child(reference, person), answer.name).await()
        }
    },
)

private fun Json.toMap() =
    (js("Object.entries")(this) as Array<Array<dynamic>>)
        .associate { it[0] as String to it[1] }

private fun <T> DataSnapshot.value() = `val`() as T

private fun DatabaseReference.asFlow() = callbackFlow {
    onValue(this@asFlow) {
        this.trySend(it)
    }
    awaitClose {

    }
}