package com.miliogo.nfce

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.miliogo.nfce.ui.theme.CurrentTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val SECRET_KEY_KEY = stringPreferencesKey("secret_key")
        val THEME_KEY = intPreferencesKey("theme")
    }

    suspend fun saveSecretKey(secretKey: String) {
        context.dataStore.edit { preferences ->
            preferences[SECRET_KEY_KEY] = secretKey
        }
    }

    suspend fun saveTheme(theme: CurrentTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.ordinal
        }
    }

    val secretKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SECRET_KEY_KEY] ?: ""
        }

    val theme: Flow<CurrentTheme> = context.dataStore.data
        .map { preferences ->
            val ordinal = preferences[THEME_KEY] ?: CurrentTheme.Default.ordinal
            CurrentTheme.entries[ordinal]
        }
}