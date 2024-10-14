package cz.rblaha15.fotbaly_ve_ctvrtek

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

interface NotificationService {
    fun scheduleNotification(
        scheduleTime: Instant,
        day: NotificationDay,
    )

    fun cancelNotification()
}

interface FirebaseDataSource {
    val answers: Flow<Map<Person, AnswerState>>
    val people: Flow<List<Person>>
    suspend fun saveAnswer(person: Person, answer: AnswerState?)
}

class Repository(
    private val settings: Settings,
    private val notificationService: NotificationService,
    private val firebaseDataSource: FirebaseDataSource,
) {
    fun getName() = settings.getStringOrNull("name")

    fun saveName(name: String) {
        settings["name"] = name
    }

    fun getAreNotificationsEnabled() = settings.getBoolean("areNotificationsEnabled", false)

    fun setNotificationsEnabled(enabled: Boolean) {
        settings["areNotificationsEnabled"] = enabled
    }

    fun cancelNotification() {
        notificationService.cancelNotification()
    }

    fun scheduleNotification(
        day: NotificationDay,
    ) {

        val currentTime = Clock.System.now()
        val inDays = (0..7).first {
            currentTime.plus(it.days).toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek == day.toKotlinxDayOfWeek()
        }
        val scheduledTime = currentTime
            .plus(inDays.days)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
            .atTime(16, 0)
            .toInstant(TimeZone.currentSystemDefault())

        notificationService.scheduleNotification(scheduledTime, day)
    }

    fun scheduleNewNotification(
        nextWeek: Boolean,
    ) = scheduleNotification(
        if (nextWeek) NotificationDay.Tuesday
        else NotificationDay.Wednesday
    )

    val answers = firebaseDataSource.answers
    val people = firebaseDataSource.people

    private suspend fun saveAnswer(
        person: Person,
        answer: AnswerState?,
    ) = firebaseDataSource.saveAnswer(person, answer)

    suspend fun saveAnswer(answer: AnswerState?) {
        saveAnswer(
            person = getName() ?: return,
            answer = answer
        )
    }
}

private fun NotificationDay.toKotlinxDayOfWeek() = when (this) {
    NotificationDay.Tuesday -> DayOfWeek.TUESDAY
    NotificationDay.Wednesday -> DayOfWeek.WEDNESDAY
}

enum class NotificationDay {
    Tuesday, Wednesday
}

enum class AnswerState {
    Yes, Maybe, No
}