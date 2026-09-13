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

//this is for recycle view for envelopes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.widget.Toast
import java.util.Calendar

class EnvelopesActivity : AppCompatActivity() {

    //start on August 2026 so the starter envelopes still show
    private var selectedYear = 2026
    private var selectedMonth = Calendar.AUGUST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_envelopes)

        //keep content from hiding behind status bar like in home screen and expenses screen
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

        //find close button
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find log out button
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find home tab button
        val homeTab = findViewById<TextView>(R.id.homeTab)

        //find expense tab button
        val expensesTab = findViewById<TextView>(R.id.expensesTab)

        //find profile tab button
        val profileTab = findViewById<TextView>(R.id.profileTab)

        //find edit envelopes button
        val editEnvelopesButton = findViewById<TextView>(R.id.editEnvelopesButton)

        //find month filter chip
        val monthFilterButton = findViewById<TextView>(R.id.monthFilterButton)

        //find evnvelope viewer
        val envelopeRecyclerView = findViewById<RecyclerView>(R.id.envelopeRecyclerView)

        //set up envelope lst
        envelopeRecyclerView.layoutManager = LinearLayoutManager(this)
        refreshEnvelopeList()

        //open side menu when user clicks
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //close side menu when user clicks
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //back to login screen when user clicks
        logOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }

        //close active screen and go home screen when user clicks
        homeTab.setOnClickListener {
            finish()
        }

        //open expenses screen when user clicks
        expensesTab.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)

            startActivity(intent)
        }

        //open profile screen when user clicks
        profileTab.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

            startActivity(intent)
        }

        //open the month dropdown instead of a calendar
        monthFilterButton.setOnClickListener {
            showMonthDropdown(monthFilterButton)
        }

        //open edit envelopes screen
        editEnvelopesButton.setOnClickListener {
            val intent = Intent(this, EditEnvelopesActivity::class.java)

            startActivity(intent)
        }
    }

    //used after adding an envelope so the list jumps to that month
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        applyFilterFromIntent(intent)
        refreshEnvelopeList()
    }

    //refresh list of envelopes when screen is visible
    override fun onResume() {
        super.onResume()

        refreshEnvelopeList()
    }

    //Figma-style month list under the gold chip
    private fun showMonthDropdown(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_month_dropdown, null)
        val monthList = popupView.findViewById<LinearLayout>(R.id.monthDropdownList)
        val months = EnvelopeTempMemory.filterMonths()

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

            row.text = EnvelopeTempMemory.monthTitle(monthPair.first, monthPair.second)

            //last row has no gap so the panel stays 179dp for the 4 Figma months
            if (index == months.lastIndex) {
                val params = row.layoutParams as ViewGroup.MarginLayoutParams

                params.bottomMargin = 0
                row.layoutParams = params
            }

            row.setOnClickListener {
                selectedYear = monthPair.first
                selectedMonth = monthPair.second

                popup.dismiss()
                refreshEnvelopeList()
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

    private fun refreshEnvelopeList() {
        val monthFilterButton = findViewById<TextView>(R.id.monthFilterButton)
        val envelopeRecyclerView = findViewById<RecyclerView>(R.id.envelopeRecyclerView)

        monthFilterButton.text = EnvelopeTempMemory.monthTitle(selectedYear, selectedMonth)

        val filtered = EnvelopeTempMemory.envelopesInMonth(selectedYear, selectedMonth)

        envelopeRecyclerView.adapter = EnvelopeAdapter(filtered)

        //list size check
        Toast.makeText(this, "List size: ${filtered.size}", Toast.LENGTH_SHORT).show()
    }
}
