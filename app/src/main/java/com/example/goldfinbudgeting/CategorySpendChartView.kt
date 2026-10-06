package com.example.goldfinbudgeting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// Horizontal bars: gold = spent, black tick = min goal, red tick = max goal
class CategorySpendChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Bar(
        val name: String,
        val spent: Double,
        val minGoal: Double,
        val maxGoal: Double
    )

    var bars: List<Bar> = emptyList()
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textSize = 36f
    }

    private val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = 30f
        textAlign = Paint.Align.RIGHT
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6E6E6")
    }

    private val spentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFC800")
    }

    private val overPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E64A19")
    }

    private val minPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 4f
    }

    private val maxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C62828")
        strokeWidth = 4f
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val rowHeight = dp(64f).toInt()
        val desired = paddingTop + paddingBottom + (bars.size.coerceAtLeast(1) * rowHeight)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, resolveSize(desired, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (bars.isEmpty()) {
            labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("No expenses in this period", paddingLeft.toFloat(), dp(24f), labelPaint)
            return
        }

        val scale = bars.maxOf { maxOf(it.spent, it.minGoal, it.maxGoal) }.coerceAtLeast(1.0)
        val rowHeight = (height - paddingTop - paddingBottom) / bars.size.toFloat()
        val barLeft = paddingLeft + dp(8f)
        val barRight = width - paddingRight - dp(8f)
        val barWidth = (barRight - barLeft).coerceAtLeast(1f)

        bars.forEachIndexed { index, bar ->
            val top = paddingTop + index * rowHeight
            val nameBaseline = top + dp(16f)
            labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(bar.name, barLeft, nameBaseline, labelPaint)

            amountPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(ExpenseLogic.formatAmount(bar.spent), barRight, nameBaseline, amountPaint)

            val trackTop = top + dp(26f)
            val trackBottom = trackTop + dp(14f)
            canvas.drawRoundRect(barLeft, trackTop, barRight, trackBottom, dp(6f), dp(6f), trackPaint)

            val spentRight = barLeft + (bar.spent / scale * barWidth).toFloat()
            val fill = if (ExpenseLogic.goalStanding(bar.spent, bar.minGoal, bar.maxGoal) ==
                ExpenseLogic.GoalStanding.ABOVE_MAXIMUM
            ) {
                overPaint
            } else {
                spentPaint
            }
            if (spentRight > barLeft) {
                canvas.drawRoundRect(barLeft, trackTop, spentRight, trackBottom, dp(6f), dp(6f), fill)
            }

            if (bar.minGoal > 0.0) {
                val x = barLeft + (bar.minGoal / scale * barWidth).toFloat()
                canvas.drawLine(x, trackTop - dp(4f), x, trackBottom + dp(4f), minPaint)
            }

            if (bar.maxGoal > 0.0) {
                val x = barLeft + (bar.maxGoal / scale * barWidth).toFloat()
                canvas.drawLine(x, trackTop - dp(4f), x, trackBottom + dp(4f), maxPaint)
            }
        }
    }
}
