package com.bignerdranch.android.deadlinetimer.ui.settings

import com.bignerdranch.android.deadlinetimer.R
import coil.compose.rememberAsyncImagePainter
import android.content.SharedPreferences
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bignerdranch.android.deadlinetimer.ui.profile.ProfileViewModel
import com.bignerdranch.android.deadlinetimer.ui.theme.AccentPrimary
import com.bignerdranch.android.deadlinetimer.ui.theme.BackgroundDark
import com.bignerdranch.android.deadlinetimer.ui.theme.SurfaceDark
import com.bignerdranch.android.deadlinetimer.ui.theme.TextPrimary
import com.bignerdranch.android.deadlinetimer.ui.theme.TextSecondary
import org.mindrot.jbcrypt.BCrypt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProfileViewModel,
    securePrefs: SharedPreferences,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Подтягиваем текущие данные пользователя для отображения по умолчанию
    val currentName = remember { securePrefs.getString("user_name", "") ?: "" }
    val currentEmail = remember { securePrefs.getString("user_email", "") ?: "" }
    var newName by remember { mutableStateOf(currentName) }

    val context = LocalContext.current
    var avatarUriString by remember {
        mutableStateOf(securePrefs.getString("user_avatar_uri", null))
    }
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(avatarUriString?.let { Uri.parse(it) })
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Вызываем метод вьюмодели, который скопирует файл и обновит StateFlow!
            viewModel.updateProfile(context, newName, it)
        }
    }



    var newEmail by remember { mutableStateOf(currentEmail) }
    var newPassword by remember { mutableStateOf("") }
    var passwordConfirmForEmailChange by remember { mutableStateOf("") }

    // Переменная для управления размером аватарки (как просил на схеме)
    var avatarSizeMultiplier by remember { mutableStateOf(120f) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                ),
                title = { Text("Настройки профиля", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back), // Убедись, что есть стрелочка назад в drawable
                            contentDescription = "Назад",
                            tint = TextPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()), // Чтобы экран можно было скроллить при открытии клавиатуры
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. АВАТАРКА С ИЗМЕНЕНИЕМ РАЗМЕРА (Реализация схемы image_26e7a9.png)
            Box(
                modifier = Modifier
                    .size(avatarSizeMultiplier.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(2.dp, AccentPrimary, CircleShape)
                    .clickable { galleryLauncher.launch("image/*") }, // Клик по самой аватарке тоже откроет галерею
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Если пользователь выбрал фото — отображаем его через Coil
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImageUri),
                        contentDescription = "Аватарка",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Если фото нет — показываем дефолтного человечка
                    Image(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Аватарка по умолчанию",
                        modifier = Modifier.fillMaxSize(0.6f),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Изменить аватарку",
                color = AccentPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // Запускаем системное окно выбора файлов/фотографий
                        galleryLauncher.launch("image/*")
                    }
                    .padding(8.dp)
            )

            // Ползунок изменения размера фотографии аватарки
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Размер:", color = TextSecondary, fontSize = 12.sp)
                Slider(
                    value = avatarSizeMultiplier,
                    onValueChange = { avatarSizeMultiplier = it },
                    valueRange = 80f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentPrimary,
                        activeTrackColor = AccentPrimary,
                        inactiveTrackColor = SurfaceDark
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. ПОЛЯ РЕДАКТИРОВАНИЯ ИМЕНИ
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it; errorMessage = null; successMessage = null },
                label = { Text("Новое имя пользователя") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary,
                    unfocusedBorderColor = Color(0xFF2D2D34)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. ПОЛЯ РЕДАКТИРОВАНИЯ ПОЧТЫ
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it; errorMessage = null; successMessage = null },
                label = { Text("Новая электронная почта") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary,
                    unfocusedBorderColor = Color(0xFF2D2D34)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ПОЛЕ ДЛЯ ВВОДА ТЕКУЩЕГО ПАРОЛЯ (Обязательно для смены почты)
            OutlinedTextField(
                value = passwordConfirmForEmailChange,
                onValueChange = { passwordConfirmForEmailChange = it; errorMessage = null; successMessage = null },
                label = { Text("Введите текущий пароль для подтверждения почты") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passwordConfirmForEmailChange.isNotBlank()) AccentPrimary else Color(0xFF2D2D34),
                    unfocusedBorderColor = Color(0xFF2D2D34)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. ПОЛЕ ДЛЯ НОВОГО ПАРОЛЯ (Если пользователь хочет обновить и его)
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; errorMessage = null; successMessage = null },
                label = { Text("Новый пароль (оставьте пустым, если не меняется)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary,
                    unfocusedBorderColor = Color(0xFF2D2D34)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Вывод уведомлений об ошибках/успехе
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }
            if (successMessage != null) {
                Text(text = successMessage!!, color = Color(0xFF10B981), fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // КНОПКА СОХРАНЕНИЯ
            Button(
                onClick = {
                    val savedPasswordHash = securePrefs.getString("user_password", "") ?: ""

                    // 1. Валидация формата почты
                    if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        errorMessage = "Некорректный формат почты"
                        return@Button
                    }

                    // 2. Если имя изменилось
                    if (newName.isBlank()) {
                        errorMessage = "Имя пользователя не может быть пустым"
                        return@Button
                    }

                    // 3. Защита: Если почта была изменена, требуем пароль
                    if (newEmail != currentEmail) {
                        if (passwordConfirmForEmailChange.isBlank()) {
                            errorMessage = "Для изменения почты необходимо ввести текущий пароль"
                            return@Button
                        }
                        if (!BCrypt.checkpw(passwordConfirmForEmailChange, savedPasswordHash)) {
                            errorMessage = "Текущий пароль введен неверно"
                            return@Button
                        }
                    }

                    // 4. Валидация и хэширование нового пароля (если его ввели)
                    var updatedPasswordHash = savedPasswordHash
                    if (newPassword.isNotBlank()) {
                        if (newPassword.length < 8) {
                            errorMessage = "Новый пароль должен быть не менее 8 символов"
                            return@Button
                        }
                        updatedPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12))
                    }

                    // Запись обновленных данных в зашифрованное хранилище
                    securePrefs.edit().apply {
                        putString("user_name", newName)
                        putString("user_email", newEmail)
                        putString("user_password", updatedPasswordHash)
                        apply()
                    }

                    successMessage = "Изменения успешно сохранены!"
                    passwordConfirmForEmailChange = ""
                    newPassword = ""
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить изменения", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}