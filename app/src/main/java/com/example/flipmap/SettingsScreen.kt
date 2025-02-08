package com.example.flipmap

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Settings Placeholder",
            modifier = Modifier
                .weight(1f)
                .wrapContentSize()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Back",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onBackClick() },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}