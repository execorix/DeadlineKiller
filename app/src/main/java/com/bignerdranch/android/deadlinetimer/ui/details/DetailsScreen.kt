package com.bignerdranch.android.deadlinetimer.ui.details

import android.app.TimePickerDialog
import android.os.Build
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bignerdranch.android.deadlinetimer.ui.theme.AccentPrimary
import com.bignerdranch.android.deadlinetimer.ui.theme.BackgroundDark
import com.bignerdranch.android.deadlinetimer.ui.theme.ColorCompleted
import com.bignerdranch.android.deadlinetimer.ui.theme.SurfaceDark
import com.bignerdranch.android.deadlinetimer.ui.theme.TextPrimary
import com.bignerdranch.android.deadlinetimer.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale
@RequiresApi(Build.VERSION_CODES.O)
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
    val scrollState = rememberScrollState()

    // Состояния для показа диалогов и выпадающих меню
    var showDatePicker by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) } // Меню выбора категорий

    var inputTitle by remember { mutableStateOf("") }

    val formatter = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }


    val availableCategories by viewModel.availableCategories.collectAsState()

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _: TimePicker, hour: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                viewModel.onDateChange(calendar.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = AccentPrimary
                ),
                title = { Text(viewModel.title.ifBlank { "Новый дедлайн" }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveDeadline(context = context, onSuccess = onBackClick) }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Сохранить", modifier = Modifier.size(28.dp))
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 100.dp) // Отступ снизу под фиксированную кнопку выполнения
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ошибка валидации
                viewModel.validationError?.let { errorText ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A1C1C)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = errorText, color = Color(0xFFEF4444), modifier = Modifier.padding(12.dp))
                    }
                }

                // Блок скругленного описания
                Text("Описание:", color = TextSecondary, fontSize = 14.sp)
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("Добавьте детали к задаче...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(16.dp)),
                    maxLines = 4,
                    textStyle = TextStyle(color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = Color(0xFF2D2D34),
                        focusedLabelColor = AccentPrimary,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                // Текстовый вывод срока сдачи
                Text(
                    text = "Срок: до ${formatter.format(Date(viewModel.endDate))}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Раздел минималистичных кнопок управления параметрами
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StyleMinimalButton(text = "Изменить срок") {
                        showDatePicker = true
                    }

                    StyleMinimalButton(text = "Изменить имя") {
                        inputTitle = viewModel.title
                        showNameDialog = true
                    }

                    val currentPriorityLabel = when(viewModel.priority) {
                        1 -> "Низкая"
                        2 -> "Средняя"
                        3 -> "Высокая"
                        else -> "Низкая"
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        StyleMinimalButton(text = "Изменить срочность ($currentPriorityLabel)") {
                            showPriorityMenu = true
                        }
                        DropdownMenu(
                            expanded = showPriorityMenu,
                            onDismissRequest = { showPriorityMenu = false },
                            modifier = Modifier
                                .background(SurfaceDark)
                                .border(1.dp, Color(0xFF2D2D34), RoundedCornerShape(8.dp))
                        ) {
                            listOf(1 to "Низкая", 2 to "Средняя", 3 to "Высокая").forEach { (level, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = TextPrimary) },
                                    onClick = {
                                        viewModel.setDeadlinePriority(level)
                                        showPriorityMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // НОВАЯ КНОПКА: Управление категориями
                    Box(modifier = Modifier.fillMaxWidth()) {
                        StyleMinimalButton(text = "Изменить категорию (${viewModel.category})") {
                            showCategoryMenu = true
                        }
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier
                                .background(SurfaceDark)
                                .border(1.dp, Color(0xFF2D2D34), RoundedCornerShape(8.dp))
                        ) {
                            availableCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = TextPrimary) },
                                    onClick = {
                                        viewModel.onCategoryChange(cat) // Записываем категорию в дедлайн
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Раздел подзадач (Сабтасков)
                if (deadlineId > 0) {
                    val subTasks by viewModel.subTasksFlow.collectAsState(initial = emptyList())

                    HorizontalDivider(color = Color(0xFF2D2D34), modifier = Modifier.padding(vertical = 8.dp))

                    Text("подзадачи:", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.newSubTaskText,
                            onValueChange = { viewModel.onNewSubTaskTextChange(it) },
                            label = { Text("Добавить мини-таск") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = Color(0xFF2D2D34),
                                focusedLabelColor = AccentPrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // Минималистичная кнопка "+" добавления сабтаска
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (viewModel.newSubTaskText.isNotBlank()) AccentPrimary else Color(0xFF2D2D34))
                                .clickable(enabled = viewModel.newSubTaskText.isNotBlank()) { viewModel.addSubTask() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Список добавленных подзадач
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subTasks.forEach { subTask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Ромбовидный или квадратный кастомный чекбокс из макета
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (subTask.isCompleted) ColorCompleted else TextSecondary,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .background(if (subTask.isCompleted) ColorCompleted.copy(alpha = 0.2f) else Color.Transparent)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.toggleSubTaskCompletion(subTask) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (subTask.isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = ColorCompleted, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = subTask.taskText,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp,
                                    textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (subTask.isCompleted) TextSecondary else TextPrimary
                                )

                                IconButton(
                                    onClick = { viewModel.deleteSubTask(subTask) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Удалить",
                                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Отдельный фиксированный блок закрытия задачи (Под всеми элементами)
            if (deadlineId > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(BackgroundDark)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(
                                width = 1.dp,
                                color = if (viewModel.isCompleted) ColorCompleted else Color(0xFF2D2D34),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.toggleDeadlineCompletion() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (viewModel.isCompleted) "Дедлайн закрыт! 🎉" else "Отметить задачу как выполненную",
                            color = if (viewModel.isCompleted) ColorCompleted else TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = if (viewModel.isCompleted) ColorCompleted else TextSecondary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(if (viewModel.isCompleted) ColorCompleted else Color.Transparent)
                                .clip(RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (viewModel.isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог изменения названия («изменить имя»)
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            title = { Text("Изменить название дедлайна", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = inputTitle,
                    onValueChange = { inputTitle = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = Color(0xFF2D2D34),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onTitleChange(inputTitle)
                        showNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                ) { Text("Принять") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Отмена", color = TextSecondary) }
            }
        )
    }

    // Календарь («изменить срок»)
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
                }) { Text("ОК", color = AccentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена", color = TextSecondary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun StyleMinimalButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, Color(0xFF2D2D34), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}