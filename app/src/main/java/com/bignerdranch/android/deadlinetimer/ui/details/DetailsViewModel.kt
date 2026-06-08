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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DetailsViewModel(private val repository: DeadlineRepository) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var endDate by mutableStateOf(System.currentTimeMillis())
        private set

    // Переменная для текста ошибки валидации
    var validationError by mutableStateOf<String?>(null)
        private set

    private var currentDeadlineId: Int = 0
    private var isEditMode = false

    var newSubTaskText by mutableStateOf("")
        private set

    // Стейт-поток, хранящий текущий ID открытого дедлайна
    private val _currentDeadlineId = MutableStateFlow(0)

    // Поток, который автоматически подгружает подзадачи при изменении ID дедлайна
    val subTasksFlow: Flow<List<SubTask>> = _currentDeadlineId.flatMapLatest { id ->
        repository.getSubTasks(id)
    }

    fun onTitleChange(newTitle: String) { title = newTitle }
    fun onDescriptionChange(newDesc: String) { description = newDesc }

    fun onNewSubTaskTextChange(text: String) { newSubTaskText = text }


    fun onDateChange(newDate: Long) {
        endDate = newDate
        validationError = null // Сбрасываем ошибку, если пользователь выбирает другое время
    }

    fun loadDeadline(id: Int) {
        if (id <= 0) return
        currentDeadlineId = id
        isEditMode = true
        _currentDeadlineId.value = id // Триггерим загрузку мини-тасков для этого ID

        viewModelScope.launch {
            repository.getDeadlineById(id).collect { deadline ->
                deadline?.let {
                    title = it.title
                    description = it.description ?: ""
                    endDate = it.endDate
                }
            }
        }
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
            newSubTaskText = "" // Очищаем поле ввода
        }
    }

    // Переключение чекбокса мини-задачи
    fun toggleSubTaskCompletion(subTask: SubTask) {
        viewModelScope.launch {
            repository.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted))
        }
    }

    // Удаление мини-задачи крестиком
    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.deleteSubTask(subTask)
        }
    }


    fun saveDeadline(context: Context, onSuccess: () -> Unit) {
        if (title.isBlank()) {
            validationError = "Название задачи не может быть пустым!"
            return
        }

        val currentTime = System.currentTimeMillis()
        if (endDate < currentTime) {
            validationError = "Нельзя установить дедлайн на прошедшее время!"
            return
        }

        viewModelScope.launch {
            validationError = null

            val deadline = Deadline(
                id = if (isEditMode) currentDeadlineId else 0,
                title = title,
                description = description.ifBlank { null },
                startDate = currentTime,
                endDate = endDate,
                priority = 1,
                isCompleted = false,
                isExtended = false
            )

            // Сохраняем или обновляем дедлайн в БД и получаем его ID
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
                        // Вешаем один тег на все уведомления этой задачи, чтобы их можно было скопом отменить
                        .addTag("deadline_$savedId")
                        .build()

                    workManager.enqueue(workRequest)
                }
            }

            onSuccess()
        }

    }
}