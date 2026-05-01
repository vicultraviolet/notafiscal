package com.miliogo.notafiscal

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val SECRET_KEY_KEY = stringPreferencesKey("secret_key")
    }

    suspend fun saveUserInput(input: String) {
        context.dataStore.edit { preferences ->
            preferences[SECRET_KEY_KEY] = input
        }
    }

    val secretKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SECRET_KEY_KEY] ?: ""
        }
}