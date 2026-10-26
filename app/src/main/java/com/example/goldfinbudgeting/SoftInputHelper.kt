package com.example.goldfinbudgeting

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Makes login-style forms usable on real phones:
 * - pads for status / nav bars and the soft keyboard (IME)
 * - scrolls the focused EditText above the keyboard
 *
 * Edge-to-edge + adjustResize alone often fails on physical devices,
 * so we handle IME insets ourselves (pair with adjustNothing in the manifest).
 */
object SoftInputHelper {

    fun setupFormScreen(activity: Activity, scrollView: ScrollView, vararg fields: EditText) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)

        val initialLeft = scrollView.paddingLeft
        val initialTop = scrollView.paddingTop
        val initialRight = scrollView.paddingRight
        val initialBottom = scrollView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            // Keyboard height already includes the nav-bar area when both are present.
            val bottom = maxOf(systemBars.bottom, ime.bottom)

            view.updatePadding(
                left = initialLeft + systemBars.left,
                top = initialTop + systemBars.top,
                right = initialRight + systemBars.right,
                bottom = initialBottom + bottom
            )

            if (ime.bottom > 0) {
                val focused = activity.currentFocus
                if (focused is EditText) {
                    view.post { scrollFieldIntoView(scrollView, focused) }
                }
            }

            windowInsets
        }

        fields.forEach { field ->
            field.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Wait for the keyboard + inset pass, then scroll.
                    scrollView.postDelayed({
                        scrollFieldIntoView(scrollView, field)
                    }, 200)
                }
            }
        }

        ViewCompat.requestApplyInsets(scrollView)
    }

    private fun scrollFieldIntoView(scrollView: ScrollView, field: View) {
        // Extra space under the field so labels / next controls aren't flush to the keyboard.
        val rect = Rect(0, 0, field.width, field.height + 64)
        scrollView.requestChildRectangleOnScreen(field, rect, true)
    }
}
