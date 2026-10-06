package com.example.goldfinbudgeting

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class SpendingActivity : AppCompatActivity() {

    private var rangeStartMillis: Long = 0L
    private var rangeEndMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spending)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val year = intent.getIntExtra("filter_year", -1)
        val month = intent.getIntExtra("filter_month", -1)
        if (year >= 0 && month >= 0) {
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(year, month, 1)
            rangeStartMillis = ExpenseTempMemory.dateOn(year, month, 1)
            rangeEndMillis = ExpenseTempMemory.dateOn(
                year,
                month,
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            )
        } else {
            val now = Calendar.getInstance()
            rangeStartMillis = ExpenseTempMemory.dateOn(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                1
            )
            rangeEndMillis = ExpenseTempMemory.dateOn(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.getActualMaximum(Calendar.DAY_OF_MONTH)
            )
        }

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.graphFromButton).setOnClickListener {
            showDatePicker(isStart = true)
        }

        findViewById<TextView>(R.id.graphToButton).setOnClickListener {
            showDatePicker(isStart = false)
        }

        refreshGraph()
        refreshPastMonth()
    }

    private fun showDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStart) rangeStartMillis else rangeEndMillis

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val picked = ExpenseTempMemory.dateOn(year, month, day)
                if (isStart) {
                    rangeStartMillis = picked
                    if (rangeStartMillis > rangeEndMillis) {
                        rangeEndMillis = picked
                    }
                } else {
                    rangeEndMillis = picked
                    if (rangeEndMillis < rangeStartMillis) {
                        rangeStartMillis = picked
                    }
                }
                refreshGraph()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun refreshGraph() {
        val fromButton = findViewById<TextView>(R.id.graphFromButton)
        val toButton = findViewById<TextView>(R.id.graphToButton)
        val chart = findViewById<CategorySpendChartView>(R.id.categorySpendChart)

        fromButton.text = "From ${ExpenseTempMemory.formatDate(rangeStartMillis)}"
        toButton.text = "To ${ExpenseTempMemory.formatDate(rangeEndMillis)}"

        val totals = ExpenseTempMemory.totalsByCategoryInRange(rangeStartMillis, rangeEndMillis)
        val envelopes = EnvelopeTempMemory.envelopes
        val names = (totals.keys + envelopes.map { it.name }).distinct()

        chart.bars = names.map { name ->
            val spent = totals.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value ?: 0.0
            val envelope = envelopes.firstOrNull { it.name.equals(name, ignoreCase = true) }
            CategorySpendChartView.Bar(
                name = name,
                spent = spent,
                minGoal = envelope?.min ?: 0.0,
                maxGoal = envelope?.max ?: 0.0
            )
        }.sortedByDescending { it.spent }
    }

    private fun refreshPastMonth() {
        val title = findViewById<TextView>(R.id.pastMonthTitle)
        val status = findViewById<TextView>(R.id.pastMonthStatus)
        val detail = findViewById<TextView>(R.id.pastMonthDetail)
        val range = findViewById<GoalRangeView>(R.id.pastMonthRange)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val spent = ExpenseTempMemory.expensesInMonth(year, month).sumOf { it.amount }
        val goals = ExpenseTempMemory.monthlyGoals(year, month)
        val minGoal = goals?.first ?: 0.0
        val maxGoal = goals?.second ?: 0.0
        val monthName = ExpenseTempMemory.monthTitle(year, month)

        title.text = "Past month · $monthName"
        range.spent = spent
        range.minGoal = minGoal
        range.maxGoal = maxGoal

        when (ExpenseLogic.goalStanding(spent, minGoal, maxGoal)) {
            ExpenseLogic.GoalStanding.NO_GOALS -> {
                status.text = "No monthly goals set"
                status.setTextColor(0xFF666666.toInt())
                detail.text = "Spent ${ExpenseTempMemory.formatAmount(spent)}. Set min and max goals for $monthName on the Expenses screen."
            }
            ExpenseLogic.GoalStanding.BELOW_MINIMUM -> {
                status.text = "Below your minimum"
                status.setTextColor(0xFFC99400.toInt())
                detail.text = goalDetail(spent, minGoal, maxGoal)
            }
            ExpenseLogic.GoalStanding.WITHIN_GOALS -> {
                status.text = "Within your goals"
                status.setTextColor(0xFF1B7A4E.toInt())
                detail.text = goalDetail(spent, minGoal, maxGoal)
            }
            ExpenseLogic.GoalStanding.ABOVE_MAXIMUM -> {
                status.text = "Above your maximum"
                status.setTextColor(0xFFE64A19.toInt())
                detail.text = goalDetail(spent, minGoal, maxGoal)
            }
        }
    }

    private fun goalDetail(spent: Double, minGoal: Double, maxGoal: Double): String {
        return "Spent ${ExpenseTempMemory.formatAmount(spent)}  ·  " +
            "Min ${ExpenseTempMemory.formatAmount(minGoal)}  ·  " +
            "Max ${ExpenseTempMemory.formatAmount(maxGoal)}"
    }
}
