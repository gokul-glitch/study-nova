package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
    }

    val loggedInUserId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LOGGED_IN_USER_ID]
        }

    suspend fun saveLoggedInUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGGED_IN_USER_ID] = userId
        }
    }

    suspend fun clearLoggedInUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(LOGGED_IN_USER_ID)
        }
    }
}
