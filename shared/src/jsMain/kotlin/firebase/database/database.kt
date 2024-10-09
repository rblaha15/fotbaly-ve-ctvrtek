@file:JsModule("firebase/database")
@file:JsNonModule

package firebase.database

import kotlin.js.Promise

external fun getDatabase(app: firebase.FirebaseApp, url: String? = definedExternally): Database

external fun ref(database: Database, path: String): DatabaseReference

external fun set(ref: DatabaseReference, value: dynamic): Promise<Unit>

external fun get(ref: DatabaseReference): Promise<DataSnapshot>

external interface Database

external interface DatabaseReference

external interface DataSnapshot {
    fun `val`(): dynamic
}

external fun onValue(ref: DatabaseReference, callback: (DataSnapshot) -> Unit)

external fun child(ref: DatabaseReference, path: String): DatabaseReference

external fun remove(ref: DatabaseReference): Promise<Unit>