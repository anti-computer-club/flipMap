package com.example.flipmap

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay


@Composable
fun LegacyTextField(
    text: String,
    onTextUpdate: (String) -> Unit,
    onTextSubmission: (String) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            EditText(ctx).apply {
                setText(text)
                setTextColor(android.graphics.Color.BLACK)
                requestFocus()

                post {
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }

                // Listen for "OK"/"Enter" key press
                setOnKeyListener { v, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_UP && keyCode == 23) {
                        Log.d("paul rocks", keyCode.toString())

                        // Trigger the callback on submission
                        onTextSubmission(text)

                        // Clear focus and hide keyboard
                        clearFocus()
                        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(windowToken, 0)
                        true
                    } else {
                        false
                    }
                }
            }
        },
        update = { editText ->
            if (editText.text.toString() != text) {
                editText.setText(text)
                editText.setSelection(text.length)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}
