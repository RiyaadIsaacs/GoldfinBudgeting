package com.example.goldfinbudgeting

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import java.util.Calendar

class ExpensesActivity : AppCompatActivity() {

    //start on August 2026 so the starter expenses still show
    private var selectedYear = 2026
    private var selectedMonth = Calendar.AUGUST

    //null means show every category for the selected month
    private var selectedCategory: String? = null

    private var searchQuery: String = ""

    //when both are set, the list uses this range instead of the month chip
    private var rangeStartMillis: Long? = null
    private var rangeEndMillis: Long? = null

    private val categoryChips = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_expenses)

        //keep content from hiding behind status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }

        applyFilterFromIntent(intent)

        //find drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        //find menu icon
        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        //find close drawer button
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find log out button
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find home button
        val homeTab = findViewById<TextView>(R.id.homeTab)

        //find profile button
        val profileTab = findViewById<TextView>(R.id.profileTab)

        //find expenses button
        val editExpensesButton = findViewById<TextView>(R.id.editExpensesButton)

        //find month filter chip
        val monthFilterButton = findViewById<TextView>(R.id.monthFilterButton)

        //find search box
        val expenseSearchEditText = findViewById<EditText>(R.id.expenseSearchEditText)

        //find date range controls
        val rangeStartButton = findViewById<TextView>(R.id.rangeStartButton)
        val rangeEndButton = findViewById<TextView>(R.id.rangeEndButton)
        val clearDateRangeButton = findViewById<TextView>(R.id.clearDateRangeButton)

        setupCategoryChips()
        updateDateRangeButtons()

        //filter the list as the user types a name, category, or note
        expenseSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString().orEmpty()
                refreshExpenseList()
            }
        })

        rangeStartButton.setOnClickListener {
            showRangeDatePicker(isStart = true)
        }

        rangeEndButton.setOnClickListener {
            showRangeDatePicker(isStart = false)
        }

        clearDateRangeButton.setOnClickListener {
            clearDateRange()
        }

        //open side menu
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //close side menu
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //go back to login screen
        logOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }

        //close expenses screen and go back to home screen. home screen is still sitting underneath expenses screen so just close screen
        homeTab.setOnClickListener {
            finish()
        }

        //open profile screen
        profileTab.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

            startActivity(intent)
        }

        //open the month dropdown instead of a calendar
        monthFilterButton.setOnClickListener {
            showMonthDropdown(monthFilterButton)
        }

        //open edit expenses screen for the month currently on screen
        editExpensesButton.setOnClickListener {
            val intent = Intent(this, EditExpensesActivity::class.java)

            intent.putExtra("filter_year", selectedYear)
            intent.putExtra("filter_month", selectedMonth)

            startActivity(intent)
        }

        refreshExpenseList()
    }

    //used after adding an expense so the list jumps to that month
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        applyFilterFromIntent(intent)
        clearDateRange(refresh = false)
        refreshExpenseList()
    }

    //refresh list of expenses when screen is visible again
    override fun onResume() {
        super.onResume()

        refreshExpenseList()
    }

    private fun setupCategoryChips() {
        val chipSubscriptions = findViewById<TextView>(R.id.chipSubscriptions)
        val chipGames = findViewById<TextView>(R.id.chipGames)
        val chipGroceries = findViewById<TextView>(R.id.chipGroceries)
        val chipTakeouts = findViewById<TextView>(R.id.chipTakeouts)

        categoryChips.clear()
        categoryChips.addAll(listOf(chipSubscriptions, chipGames, chipGroceries, chipTakeouts))

        categoryChips.forEach { chip ->
            chip.setOnClickListener {
                val category = chip.text.toString()

                //tap again to clear the filter and show the full month list
                selectedCategory = if (selectedCategory == category) {
                    null
                } else {
                    category
                }

                updateCategoryChipStyles()
                refreshExpenseList()
            }
        }

        updateCategoryChipStyles()
    }

    private fun updateCategoryChipStyles() {
        categoryChips.forEach { chip ->
            val isSelected = selectedCategory == chip.text.toString()

            if (isSelected) {
                chip.setBackgroundResource(R.drawable.rounded_chip_selected)
                chip.setTextColor(Color.WHITE)
            } else {
                chip.setBackgroundResource(R.drawable.rounded_chip)
                chip.setTextColor(Color.parseColor("#333333"))
            }
        }
    }

    private fun showRangeDatePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val currentValue = if (isStart) rangeStartMillis else rangeEndMillis

        calendar.timeInMillis = currentValue
            ?: ExpenseTempMemory.dateOn(selectedYear, selectedMonth, if (isStart) 1 else 28)

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val picked = ExpenseTempMemory.dateOn(year, month, day)

                if (isStart) {
                    rangeStartMillis = picked

                    //if the end is before the new start, nudge it forward
                    if (rangeEndMillis != null && rangeEndMillis!! < picked) {
                        rangeEndMillis = picked
                    }
                } else {
                    rangeEndMillis = picked

                    //if the start is after the new end, nudge it backward
                    if (rangeStartMillis != null && rangeStartMillis!! > picked) {
                        rangeStartMillis = picked
                    }
                }

                if (rangeStartMillis != null && rangeEndMillis != null &&
                    rangeStartMillis!! > rangeEndMillis!!
                ) {
                    Toast.makeText(this, "From date must be before To date", Toast.LENGTH_SHORT).show()
                }

                updateDateRangeButtons()
                refreshExpenseList()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun clearDateRange(refresh: Boolean = true) {
        rangeStartMillis = null
        rangeEndMillis = null
        updateDateRangeButtons()

        if (refresh) {
            refreshExpenseList()
        }
    }

    private fun updateDateRangeButtons() {
        val rangeStartButton = findViewById<TextView>(R.id.rangeStartButton)
        val rangeEndButton = findViewById<TextView>(R.id.rangeEndButton)

        rangeStartButton.text = rangeStartMillis?.let {
            "From ${ExpenseTempMemory.formatDate(it)}"
        } ?: "From date"

        rangeEndButton.text = rangeEndMillis?.let {
            "To ${ExpenseTempMemory.formatDate(it)}"
        } ?: "To date"
    }

    private fun hasActiveDateRange(): Boolean {
        return rangeStartMillis != null && rangeEndMillis != null
    }

    //Figma-style month list under the gold chip
    private fun showMonthDropdown(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_month_dropdown, null)
        val monthList = popupView.findViewById<LinearLayout>(R.id.monthDropdownList)
        val months = ExpenseTempMemory.filterMonths()

        val popup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popup.elevation = 8f
        popup.isOutsideTouchable = true

        months.forEachIndexed { index, monthPair ->
            val row = layoutInflater.inflate(R.layout.item_month_dropdown, monthList, false) as TextView

            row.text = ExpenseTempMemory.monthTitle(monthPair.first, monthPair.second)

            //last row has no gap so the panel stays tight like the envelopes dropdown
            if (index == months.lastIndex) {
                val params = row.layoutParams as ViewGroup.MarginLayoutParams

                params.bottomMargin = 0
                row.layoutParams = params
            }

            row.setOnClickListener {
                selectedYear = monthPair.first
                selectedMonth = monthPair.second

                //choosing a month exits custom range mode
                clearDateRange(refresh = false)

                popup.dismiss()
                refreshExpenseList()
            }

            monthList.addView(row)
        }

        popup.showAsDropDown(anchor, 0, 8)
    }

    private fun applyFilterFromIntent(intent: Intent) {
        if (intent.hasExtra("filter_year") && intent.hasExtra("filter_month")) {
            selectedYear = intent.getIntExtra("filter_year", selectedYear)
            selectedMonth = intent.getIntExtra("filter_month", selectedMonth)
        }
    }

    private fun refreshExpenseList() {
        val monthFilterButton = findViewById<TextView>(R.id.monthFilterButton)
        val expenseListContainer = findViewById<LinearLayout>(R.id.expenseListContainer)
        val expensesListTitle = findViewById<TextView>(R.id.expensesListTitle)
        val expensesTotalText = findViewById<TextView>(R.id.expensesTotalText)

        monthFilterButton.text = if (hasActiveDateRange()) {
            "Custom range"
        } else {
            ExpenseTempMemory.monthTitle(selectedYear, selectedMonth)
        }

        val activeStart = if (hasActiveDateRange()) rangeStartMillis else null
        val activeEnd = if (hasActiveDateRange()) rangeEndMillis else null

        val filtered = ExpenseTempMemory.expensesForScreen(
            selectedYear,
            selectedMonth,
            selectedCategory,
            searchQuery,
            activeStart,
            activeEnd
        )

        val total = ExpenseTempMemory.totalForScreen(
            selectedYear,
            selectedMonth,
            selectedCategory,
            searchQuery,
            activeStart,
            activeEnd
        )

        //title shows which slice of spending the total belongs to
        expensesListTitle.text = when {
            hasActiveDateRange() && selectedCategory != null ->
                "$selectedCategory in range"
            hasActiveDateRange() ->
                "Expenses in range"
            searchQuery.isNotBlank() && selectedCategory != null ->
                "Search in $selectedCategory"
            searchQuery.isNotBlank() ->
                "Search results"
            selectedCategory != null ->
                "$selectedCategory this month"
            else ->
                "Expenses this month"
        }

        expensesTotalText.text = ExpenseTempMemory.formatAmount(total)

        expenseListContainer.removeAllViews()

        filtered.forEachIndexed { index, expense ->
            val row = layoutInflater.inflate(R.layout.item_expense_row, expenseListContainer, false)

            val nameText = row.findViewById<TextView>(R.id.expenseNameText)
            val amountText = row.findViewById<TextView>(R.id.expenseAmountText)
            val receiptButton = row.findViewById<TextView>(R.id.expenseReceiptButton)
            val divider = row.findViewById<View>(R.id.expenseRowDivider)

            nameText.text = expense.name
            amountText.text = ExpenseTempMemory.formatAmount(expense.amount)

            if (ReceiptViewer.hasReceipt(expense.receiptPath)) {
                receiptButton.visibility = View.VISIBLE
                receiptButton.setOnClickListener {
                    ReceiptViewer.show(this, expense.receiptPath)
                }
            } else {
                receiptButton.visibility = View.GONE
                receiptButton.setOnClickListener(null)
            }

            //hide divider under the last expense so the card edge stays clean
            if (index == filtered.lastIndex) {
                divider.visibility = View.GONE
            }

            //tap a row to fix mistakes on the edit expenses screen
            row.setOnClickListener {
                val expenseIndex = ExpenseTempMemory.expenses.indexOf(expense)

                if (expenseIndex < 0) {
                    return@setOnClickListener
                }

                val calendar = Calendar.getInstance()

                calendar.timeInMillis = expense.dateCreated

                val intent = Intent(this, EditExpensesActivity::class.java)

                intent.putExtra("expense_index", expenseIndex)
                intent.putExtra("filter_year", calendar.get(Calendar.YEAR))
                intent.putExtra("filter_month", calendar.get(Calendar.MONTH))

                startActivity(intent)
            }

            expenseListContainer.addView(row)
        }
    }
}
