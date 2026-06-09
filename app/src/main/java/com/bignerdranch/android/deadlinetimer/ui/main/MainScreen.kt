package com.bignerdranch.android.deadlinetimer.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import com.bignerdranch.android.deadlinetimer.ui.theme.AccentPrimary
import com.bignerdranch.android.deadlinetimer.ui.theme.BackgroundDark
import com.bignerdranch.android.deadlinetimer.ui.theme.SurfaceDark
import com.bignerdranch.android.deadlinetimer.ui.theme.TextPrimary
import com.bignerdranch.android.deadlinetimer.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.bignerdranch.android.deadlinetimer.R


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddDeadlineClick: () -> Unit,
    onDeadlineClick: (Int) -> Unit,
    onNavigateToProfile: () -> Unit, // Оставляем для совместимости подписи сигнатуры в NavHost
    onNavigateToCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deadlines by viewModel.deadlines.collectAsState()
    val currentSortType by viewModel.sortType.collectAsState()
    val currentCategory by viewModel.selectedCategory.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    // Динамические категории из базы данных и состояние диалогового окна
    val categoriesList by viewModel.categories.collectAsState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    // Состояния для удаления категорий и задач
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteDeadlinesDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                ),
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (viewModel.isSelectionMode.value) {
                            Text("Выбрано: ${viewModel.selectedDeadlineIds.size}", fontWeight = FontWeight.SemiBold)
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Логотип",
                                modifier = Modifier
                                    .size(100.dp, 36.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (viewModel.isSelectionMode.value) {
                        IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    } else {
                        // Опциональная кнопка ручного входа в режим выделения (вместо старого гамбургера)
                        IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_checkbox),
                                contentDescription = "Выбрать задачи",
                                modifier = Modifier.size(22.dp),
                                tint = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    if (viewModel.isSelectionMode.value && viewModel.selectedDeadlineIds.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDeadlinesDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Указать на удаление", tint = Color(0xFFEF4444))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            if (!viewModel.isSelectionMode.value) {
                FloatingActionButton(
                    onClick = onAddDeadlineClick,
                    containerColor = AccentPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!viewModel.isSelectionMode.value) {
                // 1. Динамические категории дедлайнов (Скролл-бар сверху)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoriesList.forEach { category ->
                        val isCatSelected = currentCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCatSelected) AccentPrimary.copy(alpha = 0.15f) else SurfaceDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isCatSelected) AccentPrimary else Color(0xFF2D2D34),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .combinedClickable(
                                    onClick = { viewModel.changeCategory(category) },
                                    onLongClick = {
                                        if (category != "Все дедлайны") {
                                            categoryToDelete = category
                                        }
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isCatSelected) AccentPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Кнопка ПЛЮС для быстрого добавления кастомной категории
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .border(1.dp, Color(0xFF2D2D34), CircleShape)
                            .clickable { showAddCategoryDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = AccentPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 2. Единая минималистичная выпадающая кнопка сортировки
                val sortLabel = when (currentSortType) {
                    DeadlineSortType.BY_DATE -> "по дате сдачи"
                    DeadlineSortType.BY_PRIORITY -> "по важности"
                    DeadlineSortType.BY_ALPHABET -> "по алфавиту"
                }

                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .border(1.dp, Color(0xFF2D2D34), RoundedCornerShape(12.dp))
                            .clickable { showSortMenu = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Сортировка: $sortLabel",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text("▼", color = TextSecondary, fontSize = 10.sp)
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier
                            .background(SurfaceDark)
                            .border(1.dp, Color(0xFF2D2D34), RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("По дате сдачи", color = TextPrimary) },
                            onClick = {
                                viewModel.changeSortType(DeadlineSortType.BY_DATE)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По важности", color = TextPrimary) },
                            onClick = {
                                viewModel.changeSortType(DeadlineSortType.BY_PRIORITY)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По алфавиту", color = TextPrimary) },
                            onClick = {
                                viewModel.changeSortType(DeadlineSortType.BY_ALPHABET)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Список элементов
            if (deadlines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет active дедлайнов",
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(deadlines, key = { it.id }) { deadline ->
                        val isSelected = viewModel.selectedDeadlineIds.contains(deadline.id)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            DeadlineItem(
                                deadline = deadline,
                                onClick = {
                                    if (viewModel.isSelectionMode.value) {
                                        viewModel.toggleDeadlineSelection(deadline.id)
                                    } else {
                                        onDeadlineClick(deadline.id)
                                    }
                                }
                            )

                            // Наш кастомный чекбокс-ромбик/квадрат для режима выделения
                            if (viewModel.isSelectionMode.value) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 16.dp)
                                        .size(24.dp)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) AccentPrimary else TextSecondary,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .background(if (isSelected) AccentPrimary else Color.Transparent)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.toggleDeadlineSelection(deadline.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ДИАЛОГОВОЕ ОКНО СОЗДАНИЯ СВОЕЙ КАТЕГОРИИ
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddCategoryDialog = false
                newCategoryInput = ""
            },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            title = { Text("Создать категорию", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    label = { Text("Название категории") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = Color(0xFF2D2D34),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentPrimary,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryInput.isNotBlank()) {
                            viewModel.addCategory(newCategoryInput)
                            showAddCategoryDialog = false
                            newCategoryInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddCategoryDialog = false
                    newCategoryInput = ""
                }) { Text("Отмена", color = TextSecondary) }
            }
        )
    }

    // ДИАЛОГОВОЕ ОКНО ДЛЯ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ КАТЕГОРИИ
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Удалить категорию?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Категория \"$categoryToDelete\" будет полностью удалена. Все связанные с ней задачи будут перенесены в категорию \"Все дедлайны\".") },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let { viewModel.deleteCategory(it) }
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Удалить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }

    // ДИАЛОГОВОЕ ОКНО ДЛЯ МНОЖЕСТВЕННОГО УДАЛЕНИЯ ЗАДАЧ
    if (showDeleteDeadlinesDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDeadlinesDialog = false },
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Удалить задачи?", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Вы действительно хотите удалить выбранные дедлайны (${viewModel.selectedDeadlineIds.size} шт.)?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedDeadlines(deadlines)
                        viewModel.toggleSelectionMode() // Выходим из режима выделения после удаления
                        showDeleteDeadlinesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Удалить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDeadlinesDialog = false }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeadlineItem(
    deadline: Deadline,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (deadline.priority) {
        3 -> Color(0xFFEF4444)
        2 -> Color(0xFFFBBF24)
        else -> Color(0xFF10B981)
    }

    val priorityText = when (deadline.priority) {
        3 -> "Важно"
        2 -> "Средне"
        else -> "Низкая"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2D2D34), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = deadline.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(8.dp)
                            .background(borderColor, CircleShape)
                    )
                    Text(
                        text = priorityText,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatRemainingTime(deadline.endDate),
                fontSize = 13.sp,
                color = if (deadline.endDate - System.currentTimeMillis() < 3600000 * 12) Color(0xFFEF4444) else TextSecondary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

fun formatRemainingTime(endDateTimestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val remainingMillis = endDateTimestamp - currentTime

    if (remainingMillis <= 0) {
        return "Время истекло"
    }

    val totalMinutes = remainingMillis / (1000 * 60)
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    return when {
        totalHours < 1 -> "времени осталось: $totalMinutes минут(ы)"
        totalDays < 1 -> "времени осталось: $totalHours часов"
        else -> "времени осталось: $totalDays дней"
    }
}