package com.bignerdranch.android.deadlinetimer.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.deadlinetimer.data.local.datastore.ProfilePreferences
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class DeadlineStats(
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val completedPercentage: Float = 0f
)

class ProfileViewModel(
    private val repository: DeadlineRepository,
    private val context: Context
) : ViewModel() {

    // Инициализируем хранилище прямо внутри ViewModel для чистоты работы с данными
    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secure_user_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Считываем сохраненные при регистрации / редактировании данные
    private val _userName = MutableStateFlow(securePrefs.getString("user_name", "Имя пользователя") ?: "Имя пользователя")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(securePrefs.getString("user_email", "example@mail.ru") ?: "example@mail.ru")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _avatarUri = MutableStateFlow(securePrefs.getString("user_avatar_uri", null))
    val avatarUri: StateFlow<String?> = _avatarUri.asStateFlow()

    // Стейт для статистики дедлайнов (подстрой под свой класс Stats)
    private val _statsFlow = MutableStateFlow(ProfileStats())
    val statsFlow: StateFlow<ProfileStats> = _statsFlow.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getStatsFlow().collect { incomingStats ->
                _statsFlow.value = incomingStats
            }
        }
    }

    fun updateAccountData(context: Context, newName: String, newEmail: String, newPasswordHash: String?) {
        securePrefs.edit().apply {
            putString("user_name", newName)
            putString("user_email", newEmail)
            newPasswordHash?.let { putString("user_password", it) }
            apply()
        }
        _userName.value = newName
        _userEmail.value = newEmail
    }

    fun refreshData() {
        _userName.value = securePrefs.getString("user_name", "Имя пользователя") ?: "Имя пользователя"
        _userEmail.value = securePrefs.getString("user_email", "example@mail.ru") ?: "example@mail.ru"
        _avatarUri.value = securePrefs.getString("user_avatar_uri", null)
    }

    fun updateProfile(context: Context, currentName: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                val newPath = file.absolutePath
                securePrefs.edit().putString("user_avatar_uri", newPath).apply()
                _avatarUri.value = newPath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class ProfileStats(
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val completedPercentage: Float = 0f
)