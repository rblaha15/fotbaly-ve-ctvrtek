package cz.rblaha15.fotbaly_ve_ctvrtek

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

typealias Person = String

class ManViewModel(
    private val repository: Repository,
) : ViewModel() {

    private val _name = MutableStateFlow(repository.getName())
    val name = _name.asStateFlow()

    val answers = repository.answers
        .combine(name) { map, name ->
            map
                .filterKeys { it != name }
                .toList()
                .sortedBy { it.first }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), emptyList())

    fun setName(name: String) = viewModelScope.launch {
        _name.value = name
        repository.saveName(name)
    }

    private val _areNotificationsEnabled = MutableStateFlow(repository.getAreNotificationsEnabled())
    val areNotificationsEnabled = _areNotificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        _areNotificationsEnabled.value = enabled

        if (enabled)
            repository.scheduleNotification(NotificationDay.Tuesday)
        else
            repository.cancelNotification()

        repository.setNotificationsEnabled(enabled)
    }

    val myAnswer = repository.answers
        .combine(name) { answers, name ->
            answers.toList().find { it.first == name }?.second
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), null)

    fun setMyAnswer(answer: AnswerState?) = viewModelScope.launch {
        repository.saveAnswer(answer)
    }

    val count = repository.answers.map { answers ->
        answers.count { it.value == AnswerState.Yes }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), 0)
}