package com.zachupstone.walkanywhere.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 💡 Extension to create a single global dataStore instance
private val Context.dataStore by preferencesDataStore(name = "user_settings")

class DataStore(private val context: Context) {

    companion object {
        // Define a unique key for your data item
        val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    // 💡 Fetching data: Returns a dynamic Flow you can observe
    val userNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    // 💡 Saving data: A suspend function to write safely
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }
}
