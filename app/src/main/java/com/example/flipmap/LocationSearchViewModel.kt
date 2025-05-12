package com.example.flipmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flipmap.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationSearchViewModel : ViewModel() {

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            if (query.isNotBlank()) {
                try {
                    val results = RetrofitClient.api.searchLocations(query)
                    _suggestions.value = results.map { it.display_name }
                } catch (e: Exception) {
                    _suggestions.value = listOf("Error loading suggestions")
                }
            } else {
                _suggestions.value = emptyList()
            }
        }
    }
}
