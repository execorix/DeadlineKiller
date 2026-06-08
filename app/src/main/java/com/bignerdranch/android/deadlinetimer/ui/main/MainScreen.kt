package com.bignerdranch.android.deadlinetimer.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bignerdranch.android.deadlinetimer.data.local.entities.Deadline
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddDeadlineClick: () -> Unit,
    onDeadlineClick: (Int) -> Unit
) {
    // Получаем данные из БД (здесь должен быть ваш State/Flow с дедлайнами, например коллекция через collectAsState)
    val deadlines by viewModel.deadlinesFlow.collectAsState(initial = emptyList())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Наш Сайдбар (ModalNavigationDrawer)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween // Разносит контент и лого на края как в эскизе
                ) {
                    // Верхняя часть сайдбара
                    Column {
                        Text(
                            text = "Deadline Timer",
                            fontSize = 22.sp,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        HorizontalDivider()

                        // Кнопка "Выбрать"
                        NavigationDrawerItem(
                            label = { Text("Выбрать задачи") },
                            selected = viewModel.isSelectionMode.value,
                            onClick = {
                                viewModel.toggleSelectionMode()
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = {
                                Text(if (viewModel.isDarkTheme.value) "Светлая тема" else "Тёмная тема")
                            },
                            selected = false,
                            onClick = {
                                viewModel.toggleTheme()
                            }
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "v1.0 • Kolbeshkin K.A.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    ) {
        // Основной контент экрана
        Scaffold(
            topBar = {
                // Динамический TopAppBar: меняется в зависимости от режима выбора
                TopAppBar(
                    title = {
                        if (viewModel.isSelectionMode.value) {
                            Text("Выбрано: ${viewModel.selectedDeadlineIds.size}")
                        } else {
                            Text("Мои Дедлайны")
                        }
                    },
                    navigationIcon = {
                        if (viewModel.isSelectionMode.value) {
                            // Кнопка отмены выбора (крестик)
                            IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                                Icon(Icons.Default.Close, contentDescription = "Закрыть")
                            }
                        } else {
                            // Обычный бургер-меню для открытия сайдбара
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        }
                    },
                    actions = {
                        if (viewModel.isSelectionMode.value && viewModel.selectedDeadlineIds.isNotEmpty()) {
                            IconButton(onClick = { viewModel.deleteSelectedDeadlines(deadlines) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить выбранное", tint = Color.Red)
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                // Скрываем кнопку плюса в режиме выбора задач, чтобы не мешала
                if (!viewModel.isSelectionMode.value) {
                    FloatingActionButton(onClick = onAddDeadlineClick) {
                        Text("+", fontSize = 24.sp)
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(deadlines) { deadline ->
                    val isSelected = viewModel.selectedDeadlineIds.contains(deadline.id)

                    // Обертка для обработки кликов по карточке
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Наша карточка дедлайна
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

                        if (viewModel.isSelectionMode.value) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleDeadlineSelection(deadline.id) },
                                // Модификатор отвечает ТОЛЬКО за выравнивание и отступы
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 24.dp),
                                // Параметр colors идёт ОТДЕЛЬНО, после модификатора
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeadlineItem(
    deadline: Deadline,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() }
            .border(2.dp, Color.Black, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "${deadline.id}) ${deadline.title}",
                fontSize = 18.sp,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatRemainingTime(deadline.endDate),
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
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
        totalHours < 1 -> {
            "осталось времени: $totalMinutes мин."
        }
        totalDays < 1 -> {
            "осталось времени: $totalHours ч."
        }
        else -> {
            "осталось времени: $totalDays дн."
        }
    }
}