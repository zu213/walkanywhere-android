package com.zachupstone.walkanywhere.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.db.AppDatabase
import kotlinx.coroutines.launch

class SettingsViewModel: ViewModel() {

    fun deleteAllRoutes(context: Context) {
        viewModelScope.launch {
            val tripDao = AppDatabase.getInstance(context).tripDao()
            tripDao.deleteAllRoutes()
        }
    }
}