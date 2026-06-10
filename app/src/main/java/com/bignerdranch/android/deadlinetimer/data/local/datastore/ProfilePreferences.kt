package com.bignerdranch.android.deadlinetimer.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "profile_prefs")
class ProfilePreferences(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getUserName(): String = securePrefs.getString("user_name", "Имя пользователя") ?: "Имя пользователя"

    fun saveAccountData(name: String, email: String) {
        securePrefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            apply()
        }
    }
    fun getUserEmail(): String = securePrefs.getString("user_email", "example@mail.ru") ?: "example@mail.ru"

    fun saveAvatarPath(path: String) {
        securePrefs.edit().putString("user_avatar_uri", path).apply()
    }

    fun getAvatarPath(): String? = securePrefs.getString("user_avatar_uri", null)
}