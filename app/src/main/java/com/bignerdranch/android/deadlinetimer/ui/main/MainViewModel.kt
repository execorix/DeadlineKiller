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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

    private val _selectedCategory = MutableStateFlow("Все дедлайны")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()


    val deadlines: StateFlow<List<Deadline>> = combine(
        deadlinesFlow,
        sortType,
        selectedCategory
    ) { deadlinesList, type, category ->
        var filteredList = deadlinesList.filter { !it.isCompleted }

        if (category != "Все дедлайны") {
            filteredList = filteredList.filter { it.category == category }
        }

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

    val categories: StateFlow<List<String>> = repository.allCategories
        .map { categoryList ->
            listOf("Все дедлайны") + categoryList.map { it.name }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Все дедлайны")
        )

    fun addCategory(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.insertCategory(com.bignerdranch.android.deadlinetimer.data.local.entities.Category(name.trim()))
            }
        }
    }

    fun changeSortType(type: DeadlineSortType) {
        sortType.value = type
    }

    fun changeCategory(category: String) {
        _selectedCategory.value = category
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

    fun deleteCategory(categoryName: String) {
        viewModelScope.launch {
            repository.deleteCategoryAndResetDeadlines(categoryName)
            if (_selectedCategory.value == categoryName) {
                _selectedCategory.value = "Все дедлайны"
            }
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