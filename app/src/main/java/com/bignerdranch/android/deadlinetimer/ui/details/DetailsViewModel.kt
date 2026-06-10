package com.bignerdranch.android.deadlinetimer.ui.details

import android.R.attr.priority
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.data.local.entities.SubTask
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import com.bignerdranch.android.deadlinetimer.worker.NotificationWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit

class DetailsViewModel(private val repository: DeadlineRepository) : ViewModel() {

    var title by mutableStateOf("")
        private set

    var validationError by mutableStateOf<String?>(null)
        private set
    var description by mutableStateOf("")
        private set
    var endDate by mutableStateOf(System.currentTimeMillis())
        private set

    var category by mutableStateOf("Все дедлайны")
        private set
    var priority by mutableStateOf(1)
        private set
    var isCompleted by mutableStateOf(false)
        private set

    private var currentDeadlineId: Int = 0
    private var isEditMode = false

    var newSubTaskText by mutableStateOf("")
        private set



    private val _currentDeadlineId = MutableStateFlow(0)

    val subTasksFlow: Flow<List<SubTask>> = _currentDeadlineId.flatMapLatest { id ->
        repository.getSubTasks(id)
    }
    val availableCategories: StateFlow<List<String>> = repository.allCategories
        .map { list -> listOf("Все дедлайны") + list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Все дедлайны"))

    fun onDescriptionChange(newDesc: String) { description = newDesc }
    fun onNewSubTaskTextChange(text: String) { newSubTaskText = text }
    fun setDeadlinePriority(value: Int) { priority = value }

    fun onDateChange(newDate: Long) {
        endDate = newDate
        validationError = null
    }

    fun loadDeadline(id: Int) {
        if (id <= 0) return
        currentDeadlineId = id
        isEditMode = true
        _currentDeadlineId.value = id

        viewModelScope.launch {
            if (id > 0) {
                repository.getDeadlineById(id).collect { deadline ->
                    deadline?.let {
                        title = it.title
                        description = it.description ?: ""
                        endDate = it.endDate
                        priority = it.priority
                        isCompleted = it.isCompleted
                    }
                }
            }else {
                val activeCount = repository.getActiveDeadlinesCount()
                title = "Дедлайн ${activeCount + 1}"
                description = ""
                endDate = System.currentTimeMillis()
                priority = 1
                category = "Все дедлайны"
                isCompleted = false
            }
        }
    }

    fun toggleDeadlineCompletion() {
        if (currentDeadlineId <= 0 && !isEditMode) return
        isCompleted = !isCompleted
        viewModelScope.launch {
            val currentDeadline = Deadline(
                id = currentDeadlineId,
                title = title,
                description = description,
                endDate = endDate,
                priority = priority,
                isCompleted = isCompleted,
            )
            repository.updateDeadline(currentDeadline)
        }
    }

    fun onTitleChange(newTitle: String) {
        title = newTitle
    }

    fun addSubTask() {
        if (newSubTaskText.isBlank() || currentDeadlineId <= 0) return
        viewModelScope.launch {
            val subTask = SubTask(
                subTaskId = 0,
                parentDeadlineId = currentDeadlineId,
                taskText = newSubTaskText.trim(),
                isCompleted = false
            )
            repository.insertSubTask(subTask)
            newSubTaskText = ""
        }
    }

    fun toggleSubTaskCompletion(subTask: SubTask) {
        viewModelScope.launch {
            repository.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted))
        }
    }

    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
        }
    }

    fun onCategoryChange(newCategory: String) {
        category = newCategory
    }

    fun saveDeadline(context: Context, onSuccess: () -> Unit) {
        if (title.trim().isBlank()) {
            title = "Unnamed Deadline"
        }

        validationError = null

        val currentTime = System.currentTimeMillis()
        if (endDate < currentTime && !isCompleted) {
            endDate = currentTime + 603000
        }

        viewModelScope.launch {
            validationError = null

            val deadline = Deadline(
                id = if (currentDeadlineId > 0) currentDeadlineId else 0,
                title = title.trim(),
                description = description,
                endDate = endDate,
                priority = priority,
                category = category,
                isCompleted = isCompleted
            )

            val savedId = if (isEditMode) {
                repository.updateDeadline(deadline)
                currentDeadlineId
            } else {
                repository.insertDeadline(deadline)
            }


            val workManager = WorkManager.getInstance(context)

            if (isEditMode) {
                workManager.cancelAllWorkByTag("deadline_$savedId")
            }

            val notificationIntervals = listOf(
                TimeUnit.DAYS.toMillis(7) to "Осталась неделя до дедлайна!",
                TimeUnit.DAYS.toMillis(3) to "Осталось 3 дня до дедлайна!",
                TimeUnit.DAYS.toMillis(1) to "Остался 1 день до дедлайна!",
                TimeUnit.HOURS.toMillis(12) to "Осталось 12 часов до дедлайна!",
                TimeUnit.HOURS.toMillis(6) to "Осталось 6 часов до дедлайна!",
                TimeUnit.HOURS.toMillis(1) to "Остался 1 час до дедлайна!",
                TimeUnit.MINUTES.toMillis(30) to "Осталось полчаса до конца дедлайна!",
                TimeUnit.MINUTES.toMillis(10) to "Осталось 10 минут до конца дедлайна!",
                0L to "Время истекло! Дедлайн гори-и-ит!"
            )

            for ((timeBeforeDeadline, message) in notificationIntervals) {
                val targetTime = endDate - timeBeforeDeadline
                val delayInSeconds = (targetTime - currentTime) / 1000

                if (delayInSeconds > 0) {
                    val inputData = Data.Builder()
                        .putString("DEADLINE_TITLE", title)
                        .putString("DEADLINE_DESC", message)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInputData(inputData)
                        .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
                        .addTag("deadline_$savedId")
                        .build()

                    workManager.enqueue(workRequest)
                }
            }

            onSuccess()
        }
    }
}