package com.bignerdranch.android.deadlinetimer.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.filter

class MainViewModel(private val repository: DeadlineRepository) : ViewModel() {
    var isDarkTheme = mutableStateOf(false)
        private set
    val deadlinesFlow = repository.allDeadline
    var isSelectionMode = mutableStateOf(false)
        private set

    var selectedDeadlineIds = mutableStateListOf<Int>()
        private set

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    fun toggleSelectionMode() {
        isSelectionMode.value = !isSelectionMode.value
        if (!isSelectionMode.value) {
            selectedDeadlineIds.clear()
        }
    }

    fun toggleDeadlineSelection(id: Int) {
        if (selectedDeadlineIds.contains(id)) {
            selectedDeadlineIds.remove(id)
        } else {
            selectedDeadlineIds.add(id)
        }
    }

    fun deleteSelectedDeadlines(allDeadlines: List<Deadline>) {
        viewModelScope.launch {
            val toDelete = allDeadlines.filter { selectedDeadlineIds.contains(it.id) }

            toDelete.forEach { deadline ->
                repository.deleteDeadline(deadline)
            }
            selectedDeadlineIds.clear()
            isSelectionMode.value = false

            // Наш прошлый код сброса автоинкремента, если всё пусто
            if (repository.getDeadlinesCount() == 0) {
                repository.resetIdSequence()
            }
        }
    }
    val deadlinesState: StateFlow<List<Deadline>> = repository.allDeadline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteDeadline(deadline: Deadline) {
        viewModelScope.launch {
            // 1. Удаляем сам дедлайн из базы
            repository.deleteDeadline(deadline)

            // 2. Проверяем, сколько дедлайнов осталось в таблице
            val remainingCount = repository.getDeadlinesCount()

            // 3. Если список пуст — сбрасываем историю ID в ноль
            if (remainingCount == 0) {
                repository.resetIdSequence()
            }
        }
    fun toggleDeadlineCompletion(deadline: Deadline) {
        viewModelScope.launch {
            val updatedDeadline = deadline.copy(isCompleted = !deadline.isCompleted)
            repository.updateDeadline(updatedDeadline)
            }
        }
    }
}