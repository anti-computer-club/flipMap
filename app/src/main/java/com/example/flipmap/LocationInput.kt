package com.example.flipmap


import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LocationInputDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (showDialog) {
        var locationInput by remember { mutableStateOf("") }
        val viewModel: LocationSearchViewModel = viewModel()
        val suggestions by viewModel.suggestions.collectAsState()

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Enter Location") },
            text = {
                Column {
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = {
                            locationInput = it
                            viewModel.onQueryChanged(it) 
                        },
                        placeholder = { Text("Search for a location...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (suggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        suggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        locationInput = suggestion
                                        viewModel.onQueryChanged(suggestion)
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
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
