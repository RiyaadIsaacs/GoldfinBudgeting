package com.example.goldfinbudgeting

import android.graphics.Color
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

// Applies the shared gold gradient to action buttons and dialog buttons
object GoldButtons {

    fun style(button: Button) {
        button.background = ContextCompat.getDrawable(button.context, R.drawable.rounded_button_gold)
        button.backgroundTintList = null
        button.setTextColor(Color.WHITE)
    }

    // call after dialog.show or inside setOnShowListener
    fun styleShown(dialog: AlertDialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.let { style(it) }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.let { style(it) }
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.let { style(it) }
    }

    // styles Confirm Cancel Got it and similar buttons when the dialog opens
    fun styleAlert(dialog: AlertDialog) {
        dialog.setOnShowListener {
            styleShown(dialog)
        }
    }
}
