package com.example.flipmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
@Deprecated("We don't use this any more and kind of never did. Maybe add tileservers?")
fun SettingsScreen() {
    val activity = LocalContext.current as MainActivity
    val isDarkMode = activity.themeViewModel.isDarkMode.value

    val selectedPurple = Color(0xFF8B4DC6)
    val unselectedLightPurple = Color(0xFFF3EAFB)
    val unselectedDarkPurple = Color(0xFF3D2F47)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Light Mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color.White else Color.Black,
                    modifier = Modifier
                        .background(
                            color = if (!isDarkMode) selectedPurple else if (isDarkMode) unselectedDarkPurple else unselectedLightPurple,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activity.themeViewModel.toggleTheme(false) }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkMode) Color.White else Color.Black,
                    modifier = Modifier
                        .background(
                            color = if (isDarkMode) selectedPurple else if (!isDarkMode) unselectedLightPurple else unselectedDarkPurple,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activity.themeViewModel.toggleTheme(true) }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}