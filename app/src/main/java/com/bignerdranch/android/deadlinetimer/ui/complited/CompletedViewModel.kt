package com.bignerdranch.android.deadlinetimer.ui.complited

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CompletedViewModel(private val repository: DeadlineRepository) : ViewModel() {

    // Получаем только выполненные дедлайны
    val completedDeadlines: StateFlow<List<Deadline>> = repository.allDeadline
        .map { deadlines -> deadlines.filter { it.isCompleted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun restoreDeadline(deadline: Deadline) {
        viewModelScope.launch {
            repository.updateDeadline(deadline.copy(isCompleted = false))
        }
    }

    fun deleteDeadline(deadline: Deadline) {
        viewModelScope.launch {
            repository.deleteDeadline(deadline)
        }
    }
}