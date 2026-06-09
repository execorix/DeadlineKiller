package com.bignerdranch.android.deadlinetimer.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.deadlinetimer.data.local.datastore.ProfilePreferences
import com.bignerdranch.android.deadlinetimer.data.repository.DeadlineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    context: Context
) : ViewModel() {

    private val profilePreferences = ProfilePreferences(context)

    val userName: StateFlow<String> = profilePreferences.userNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Загрузка..."
    )

    val avatarUri: StateFlow<String?> = profilePreferences.avatarUriFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val statsFlow: StateFlow<DeadlineStats> = repository.allDeadline.map { list ->
        val completed = list.count { it.isCompleted }
        val active = list.count { !it.isCompleted }
        val total = list.size
        val percent = if (total > 0) (completed.toFloat() / total.toFloat()) * 100f else 0f

        DeadlineStats(completedCount = completed, activeCount = active, completedPercentage = percent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DeadlineStats()
    )
    fun updateProfile(context: Context, newName: String, uri: Uri?) {
        viewModelScope.launch {
            val finalPath = uri?.let { saveImageToInternalStorage(context, it) } ?: avatarUri.value
            profilePreferences.saveProfile(newName, finalPath)
        }
    }
    private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "user_avatar.jpg")
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}