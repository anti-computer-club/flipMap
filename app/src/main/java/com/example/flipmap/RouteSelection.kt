package com.example.flipmap

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Text entry for phones with old-school hardware keyboards.
 * This is necessary to make text focusing work properly. I think.
 *
 * @param text Starting text
 * @param onTextSubmission Callback for submission of the field. Returns the contents of the field
 */
@Composable
fun LegacyTextField(
    text: String,
    onTextSubmission: (String) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            EditText(ctx).apply {
                setText(text)
                setTextColor(android.graphics.Color.BLACK)
                requestFocus()
                // Log.d("TextField", "Factory running")

                post {
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }

                setOnKeyListener { v, keyCode, event ->
                    // ACTION_DOWN closes the hardware keyboard (iirc)
                    // so we catch the ACTION_UP to trigger the callback
                    if (event.action == KeyEvent.ACTION_UP && keyCode == 23) {
                        onTextSubmission(this.text.toString())
                        clearFocus()
                        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(windowToken, 0)
                        true
                    } else false
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
            .background(Color.White)
            .padding(2.dp)
    )
}