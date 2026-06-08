package com.bignerdranch.android.deadlinetimer.ui.details

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    deadlineId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(deadlineId) {
        viewModel.loadDeadline(deadlineId)
    }
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    // Изменили формат вывода, чтобы отображать часы и минуты в кнопке срока
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }

    // Календарь для сборки таймстампа (дата из DatePicker + время из TimePicker)
    val calendar = remember { Calendar.getInstance() }

    // Инициализируем диалог выбора времени
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _: TimePicker, hour: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // Передаем итоговый таймстамп во ViewModel
                viewModel.onDateChange(calendar.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-часовой формат
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (deadlineId > 0) "Редактировать" else "Новый дедлайн") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveDeadline(context = context, onSuccess = onBackClick) }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Сохранить")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            viewModel.validationError?.let { errorText ->
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("Название задачи") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )

            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Срок: ${formatter.format(Date(viewModel.endDate))}")
            }
            if (deadlineId > 0) { // Показываем подзадачи только для уже созданных дедлайнов
                val subTasks by viewModel.subTasksFlow.collectAsState(initial = emptyList())

                Text(
                    text = "Подзадачи:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Строка ввода новой подзадачи + кнопка "+"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.newSubTaskText,
                        onValueChange = { viewModel.onNewSubTaskTextChange(it) },
                        label = { Text("Добавить мини-таск") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.addSubTask() },
                        enabled = viewModel.newSubTaskText.isNotBlank()
                    ) {
                        Text("+")
                    }
                }

                // Список подзадач с чекбоксами
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subTasks.forEach { subTask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subTask.isCompleted,
                                onCheckedChange = { viewModel.toggleSubTaskCompletion(subTask) }
                            )

                            Text(
                                text = subTask.taskText,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                // Если задача выполнена — слегка зачеркиваем текст
                                textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (subTask.isCompleted) Color.Gray else Color.Unspecified
                            )

                            IconButton(onClick = { viewModel.deleteSubTask(subTask) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить подзадачу",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.endDate)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                        calendar.timeInMillis = selectedMillis

                        val currentTaskCalendar = Calendar.getInstance().apply { timeInMillis = viewModel.endDate }
                        calendar.set(Calendar.HOUR_OF_DAY, currentTaskCalendar.get(Calendar.HOUR_OF_DAY))
                        calendar.set(Calendar.MINUTE, currentTaskCalendar.get(Calendar.MINUTE))

                        showDatePicker = false
                        timePickerDialog.show()
                    }
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}