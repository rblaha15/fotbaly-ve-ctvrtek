package cz.rblaha15.fotbaly_ve_ctvrtek

import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

object AndroidFirebaseDataSource : FirebaseDataSource {

    private val database = Firebase.database("https://fotbaly-ve-ctvrtek-default-rtdb.europe-west1.firebasedatabase.app/")
    private val roomReference = database.getReference("rooms/cutani_u_jelena")
    private val answersReference = roomReference.child("attendance")
    private val peopleReference = roomReference.child("names")

    override suspend fun saveAnswer(person: Person, answer: AnswerState?) {
        if (answer == null)
            answersReference.child(person).removeValue().await()
        else
            answersReference.child(person).setValue(answer).await()
    }

    override val answers = answersReference.asFlow().map { snapshot ->
        snapshot.getValue<Map<Person, AnswerState>?>() ?: emptyMap()
    }

    override val people = peopleReference.asFlow().map { snapshot ->
        snapshot.getValue<List<Person>?>() ?: emptyList()
    }
}

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