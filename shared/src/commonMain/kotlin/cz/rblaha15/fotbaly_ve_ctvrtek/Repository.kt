package cz.rblaha15.fotbaly_ve_ctvrtek

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanStateFlow
import com.russhwolf.settings.coroutines.getStringOrNullStateFlow
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface NotificationService {
    fun scheduleNotification(
        scheduleTime: Instant,
        extras: Map<String, String>,
    )

    fun cancelNotification()

    fun dismissNotification()

    fun alarmsEnabled(): Boolean
}

interface FirebaseDataSource {
    val answers: Flow<Map<Person, AnswerState>>
    val people: Flow<List<Person>>
    suspend fun saveAnswer(person: Person, answer: AnswerState?)
}

@OptIn(ExperimentalSettingsApi::class, ExperimentalTime::class)
class Repository(
    private val settings: ObservableSettings,
    private val notificationService: NotificationService,
    private val firebaseDataSource: FirebaseDataSource,
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun getName() = settings.getStringOrNull("name")
    val name = settings.getStringOrNullStateFlow(scope, "name")
    fun saveName(name: String) {
        settings["name"] = name
    }

    val areNotificationsEnabled =
        settings.getBooleanStateFlow(scope, "areNotificationsEnabled", false)

    fun setNotificationsEnabled(enabled: Boolean) {
        settings["areNotificationsEnabled"] = enabled
    }

    fun cancelNotification() = notificationService.cancelNotification()
    fun dismissNotification() = notificationService.dismissNotification()

    fun alarmsEnabled() = notificationService.alarmsEnabled()

    fun scheduleNearestNotification() {
        val currentTime = Clock.System.now()
        val datetime = currentTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = if (datetime.dayOfWeek == DayOfWeek.TUESDAY && datetime.hour >= 16 || datetime.dayOfWeek == DayOfWeek.WEDNESDAY && datetime.hour < 16)
            NotificationDay.Wednesday else NotificationDay.Tuesday
        scheduleNotification(day)
    }

    fun scheduleNotification(
        day: NotificationDay,
    ) {
        val currentTime = Clock.System.now()
        val start =
            if (currentTime.toLocalDateTime(TimeZone.currentSystemDefault()).hour >= 16) 1 else 0
        val date = (start..7).asSequence().map {
            currentTime
                .plus(it.days)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        }.first {
            it.dayOfWeek == day.toKotlinxDayOfWeek()
        }
        val scheduledTime = date
            .atTime(16, 0)
            .toInstant(TimeZone.currentSystemDefault())
//        val scheduledTime = currentTime.plus(20.seconds)

        notificationService.scheduleNotification(scheduledTime, mapOf("day" to day.name))
    }

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