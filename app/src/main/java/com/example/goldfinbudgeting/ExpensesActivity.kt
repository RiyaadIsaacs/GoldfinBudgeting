package com.example.goldfinbudgeting

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
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

        setupCategoryChips()

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

        monthFilterButton.text = ExpenseTempMemory.monthTitle(selectedYear, selectedMonth)

        val filtered = ExpenseTempMemory.expensesForScreen(
            selectedYear,
            selectedMonth,
            selectedCategory
        )

        expenseListContainer.removeAllViews()

        filtered.forEachIndexed { index, expense ->
            val row = layoutInflater.inflate(R.layout.item_expense_row, expenseListContainer, false)

            val nameText = row.findViewById<TextView>(R.id.expenseNameText)
            val amountText = row.findViewById<TextView>(R.id.expenseAmountText)
            val divider = row.findViewById<View>(R.id.expenseRowDivider)

            nameText.text = expense.name
            amountText.text = ExpenseTempMemory.formatAmount(expense.amount)

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

                val intent = Intent(this, EditExpensesActivity::class.java)

                intent.putExtra("expense_index", expenseIndex)
                intent.putExtra("filter_year", selectedYear)
                intent.putExtra("filter_month", selectedMonth)

                startActivity(intent)
            }

            expenseListContainer.addView(row)
        }
    }
}
