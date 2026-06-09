package com.bignerdranch.android.deadlinetimer.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "profile_prefs")

class ProfilePreferences(private val context: Context) {

    companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val AVATAR_URI_KEY = stringPreferencesKey("avatar_uri")
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY] ?: "Имя пользователя"
    }

    val avatarUriFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[AVATAR_URI_KEY]
    }

    suspend fun saveProfile(name: String, avatarUri: String?) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME_KEY] = name
            if (avatarUri != null) {
                prefs[AVATAR_URI_KEY] = avatarUri
            }
        }
    }
}