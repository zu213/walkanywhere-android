package com.zachupstone.walkanywhere.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.AppDatabase
import kotlinx.coroutines.launch

class SettingsViewModel: ViewModel() {

    fun deleteAllRoutes(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            tripDao.deleteAllRoutes()
        }
    }
}