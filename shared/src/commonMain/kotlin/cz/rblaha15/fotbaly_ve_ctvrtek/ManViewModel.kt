package cz.rblaha15.fotbaly_ve_ctvrtek

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

typealias Person = String

class ManViewModel(
    private val repository: Repository,
) : ViewModel() {
    val name = repository.name

    val answers = repository.answers
        .combine(name) { map, name ->
            map
                .toList()
                .sortedBy { it.first }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyList())

    val people = repository.people
        .map { names ->
            names.sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyList())

    fun setName(name: String) = viewModelScope.launch {
        repository.saveName(name)
    }

    fun alarmsEnabled() = repository.alarmsEnabled()

    val areNotificationsEnabled = repository.areNotificationsEnabled

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        if (enabled)
            repository.scheduleNearestNotification()
        else {
            repository.dismissNotification()
            repository.cancelNotification()
        }

        repository.setNotificationsEnabled(enabled)
    }

    val myAnswer = repository.answers
        .combine(name) { answers, name ->
            answers.toList().find { it.first == name }?.second
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    fun setMyAnswer(answer: AnswerState?) = viewModelScope.launch {
        repository.saveAnswer(answer)
        repository.dismissNotification()
    }

    val count = repository.answers.map { answers ->
        answers.count { it.value == AnswerState.Yes }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), 0)
}