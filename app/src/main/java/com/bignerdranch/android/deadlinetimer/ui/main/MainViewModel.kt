package com.bignerdranch.android.deadlinetimer.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.filter


class MainViewModel(private val repository: DeadlineRepository) : ViewModel() {

    val deadlinesFlow = repository.allDeadline

    var isSelectionMode = mutableStateOf(false)
        private set

    var selectedDeadlineIds = mutableStateListOf<Int>()
        private set

    val sortType = MutableStateFlow(DeadlineSortType.BY_DATE)

    // Текущая выбранная категория (пока заглушка для верстки)
    val selectedCategory = MutableStateFlow("Все дедлайны")

    // Фильтруем только НЕвыполненные дедлайны и сортируем их
    val deadlines: StateFlow<List<Deadline>> = combine(
        deadlinesFlow,
        sortType,
        selectedCategory // Наш MutableStateFlow("Все дедлайны")
    ) { deadlinesList, type, category ->
        // Сначала отсекаем выполненные
        var filteredList = deadlinesList.filter { !it.isCompleted }

        // Затем фильтруем по выбранной категории (если выбрано не "Все дедлайны")
        if (category != "Все дедлайны") {
            filteredList = filteredList.filter { it.category == category }
        }

        // В конце применяем сортировку
        when (type) {
            DeadlineSortType.BY_DATE -> filteredList.sortedBy { it.endDate }
            DeadlineSortType.BY_PRIORITY -> filteredList.sortedByDescending { it.priority }
            DeadlineSortType.BY_ALPHABET -> filteredList.sortedBy { it.title.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun changeSortType(type: DeadlineSortType) {
        sortType.value = type
    }

    fun changeCategory(category: String) {
        selectedCategory.value = category
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

            if (repository.getDeadlinesCount() == 0) {
                repository.resetIdSequence()
            }
        }
    }
}