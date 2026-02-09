package com.idedi.perkiraancuacalaut.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.idedi.perkiraancuacalaut.data.ForecastRepository

class MainViewModelFactory(
    private val repository: ForecastRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
