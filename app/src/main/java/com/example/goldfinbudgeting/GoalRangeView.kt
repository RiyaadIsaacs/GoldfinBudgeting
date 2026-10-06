package com.example.goldfinbudgeting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// Shows spent against a shaded min-to-max goal band for one period
class GoalRangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var spent: Double = 0.0
        set(value) {
            field = value
            invalidate()
        }

    var minGoal: Double = 0.0
        set(value) {
            field = value
            invalidate()
        }

    var maxGoal: Double = 0.0
        set(value) {
            field = value
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6E6E6")
    }

    private val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7DDFC3")
    }

    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A200")
    }

    private val overPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E64A19")
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (dp(36f) + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            resolveSize(desired, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val left = paddingLeft + dp(8f)
        val right = width - paddingRight - dp(8f)
        val centerY = height / 2f
        val trackTop = centerY - dp(6f)
        val trackBottom = centerY + dp(6f)
        val widthPx = (right - left).coerceAtLeast(1f)
        val scale = maxOf(spent, minGoal, maxGoal, 1.0)

        canvas.drawRoundRect(left, trackTop, right, trackBottom, dp(6f), dp(6f), trackPaint)

        if (maxGoal > minGoal && maxGoal > 0.0) {
            val bandLeft = left + (minGoal / scale * widthPx).toFloat()
            val bandRight = left + (maxGoal / scale * widthPx).toFloat()
            canvas.drawRoundRect(bandLeft, trackTop, bandRight, trackBottom, dp(6f), dp(6f), bandPaint)
        }

        val markerX = left + (spent / scale * widthPx).toFloat()
        val standing = ExpenseLogic.goalStanding(spent, minGoal, maxGoal)
        val paint = if (standing == ExpenseLogic.GoalStanding.ABOVE_MAXIMUM) overPaint else markerPaint
        canvas.drawCircle(markerX, centerY, dp(8f), paint)
    }
}
