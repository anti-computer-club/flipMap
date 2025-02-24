package com.example.flipmap.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class ThemeViewModel {
    private val _isDarkMode = mutableStateOf(true)
    val isDarkMode: State<Boolean> = _isDarkMode

    fun toggleTheme(isDark: Boolean) {
        _isDarkMode.value = isDark
    }
}