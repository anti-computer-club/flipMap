package com.example.flipmap

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LocationInputDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (showDialog) {
        var locationInput by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Enter Location") },
            text = {
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    placeholder = { Text(" ") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(locationInput)
                    onDismiss()
                }) {
                    Text("Search")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}