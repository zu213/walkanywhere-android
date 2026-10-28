package com.zachupstone.walkanywhere.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zachupstone.walkanywhere.data.db.AppDatabase
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application): AndroidViewModel(application) {
    private val tripDao = AppDatabase.getInstance(application).tripDao()

    fun deleteAllRoutes() {
        viewModelScope.launch {
            tripDao.deleteAllRoutes()
        }
    }
}