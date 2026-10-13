package com.example.goldfinbudgeting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin

// Vertical bar graph: gold bar = spent, black line = min goal, red line = max goal
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
        textSize = sp(11f)
    }

    private val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = sp(9f)
        textAlign = Paint.Align.CENTER
    }

    private val axisLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        textSize = sp(10f)
        textAlign = Paint.Align.RIGHT
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6E6E6")
        strokeWidth = dp(1f)
    }

    private val spentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFC800")
    }

    private val overPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E64A19")
    }

    private val minPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = dp(2.5f)
        strokeCap = Paint.Cap.ROUND
    }

    private val maxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C62828")
        strokeWidth = dp(2.5f)
        strokeCap = Paint.Cap.ROUND
    }

    private val barRect = RectF()

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            resources.displayMetrics
        )
    }

    private fun axisAmount(value: Double): String {
        return "R %,d".format(Locale.US, round(value).toLong())
    }

    private fun longestLabelWidth(): Float {
        return bars.maxOfOrNull { labelPaint.measureText(it.name) } ?: 0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val labelDrop = longestLabelWidth() * sin(Math.toRadians(40.0)).toFloat() + dp(20f)
        val desired = paddingTop + paddingBottom + dp(210f).toInt() + labelDrop.toInt()
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

        val scale = bars.maxOf { max(it.spent, max(it.minGoal, it.maxGoal)) }.coerceAtLeast(1.0)
        val axisLabels = (0..4).map { axisAmount(scale * it / 4.0) }
        val longest = longestLabelWidth()
        val labelDrop = longest * sin(Math.toRadians(40.0)).toFloat() + dp(16f)
        val labelReach = longest * cos(Math.toRadians(40.0)).toFloat()
        val axisWidth = axisLabels.maxOf { axisLabelPaint.measureText(it) }

        val plotLeft = paddingLeft + axisWidth + dp(8f)
        val plotRight = width - paddingRight - min(labelReach * 0.55f, dp(72f))
        val plotTop = paddingTop + dp(16f)
        val plotBottom = height - paddingBottom - labelDrop
        val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)
        val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)

        for (step in 0..4) {
            val fraction = step / 4f
            val y = plotBottom - fraction * plotHeight
            canvas.drawLine(plotLeft, y, plotRight, y, gridPaint)
            canvas.drawText(
                axisLabels[step],
                plotLeft - dp(6f),
                y + axisLabelPaint.textSize * 0.35f,
                axisLabelPaint
            )
        }

        val slot = plotWidth / bars.size
        val barWidth = (slot * 0.5f).coerceAtLeast(dp(6f)).coerceAtMost(dp(36f))

        bars.forEachIndexed { index, bar ->
            val center = plotLeft + slot * index + slot / 2f
            val left = center - barWidth / 2f
            val right = center + barWidth / 2f
            val barHeight = (bar.spent / scale * plotHeight).toFloat()
            val top = plotBottom - barHeight

            if (barHeight > 0f) {
                val fill = if (ExpenseLogic.goalStanding(bar.spent, bar.minGoal, bar.maxGoal) ==
                    ExpenseLogic.GoalStanding.ABOVE_MAXIMUM
                ) {
                    overPaint
                } else {
                    spentPaint
                }
                val radius = min(dp(5f), min(barWidth / 2f, barHeight / 2f))
                barRect.set(left, top, right, plotBottom)
                canvas.drawRoundRect(barRect, radius, radius, fill)
                if (barHeight > radius) {
                    canvas.drawRect(left, plotBottom - radius, right, plotBottom, fill)
                }
            }

            if (bar.minGoal > 0.0) {
                val y = plotBottom - (bar.minGoal / scale * plotHeight).toFloat()
                canvas.drawLine(left - dp(3f), y, right + dp(3f), y, minPaint)
            }

            if (bar.maxGoal > 0.0) {
                val y = plotBottom - (bar.maxGoal / scale * plotHeight).toFloat()
                canvas.drawLine(left - dp(3f), y, right + dp(3f), y, maxPaint)
            }

            val amountY = if (barHeight > dp(14f)) top - dp(3f) else plotBottom - dp(4f)
            canvas.drawText(ExpenseLogic.formatAmount(bar.spent), center, amountY, amountPaint)

            canvas.save()
            val labelX = center - barWidth / 2f
            val labelY = plotBottom + dp(14f)
            canvas.rotate(-40f, labelX, labelY)
            labelPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(bar.name, labelX, labelY, labelPaint)
            canvas.restore()
        }
    }
}
