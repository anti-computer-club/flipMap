package com.example.flipmap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SoftKeyNavBar(leftClick: String, centerClick: String, rightClick: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .focusable(false),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf(leftClick, centerClick, rightClick).forEach { text ->
            Text(
                text = text,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(enabled = false) {} // disables focus + click
                    .focusable(false)
            )
        }
    }
}