package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout

//recycler view
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    // Total of all expenses in the current calendar month
    private var moneySpentThisMonth: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        //keep content from hiding behind status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }

        //find side drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        //find hamburger menu icon
        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        //find close button
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find logout button
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find expenses tab
        val expensesTab = findViewById<TextView>(R.id.expensesTab)

        //find profile tab
        val profileTab = findViewById<TextView>(R.id.profileTab)

        //find envelope list
        val envelopeRecyclerView = findViewById<RecyclerView>(R.id.envelopeRecyclerView)

        //setup envelope list with adapter
        envelopeRecyclerView.layoutManager = LinearLayoutManager(this)
        envelopeRecyclerView.adapter = EnvelopeAdapter(EnvelopeTempMemory.envelopes)

        // Show the monthly total 
        refreshHomeTotals()

        //open side menu
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //close side menu
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //go back to log in screen
        logOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }

        //open expenses screen
        expensesTab.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)

            startActivity(intent)
        }

        //open profile screen
        profileTab.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

            startActivity(intent)
        }

        //find envelopes in the drawer
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)

        //open envelopes screen
        envelopesMenuItem.setOnClickListener {
            val intent = Intent(this, EnvelopesActivity::class.java)

            startActivity(intent)
        }
    }

    //refresh envelope list and monthly total when screen is visible again
    override fun onResume() {
        super.onResume()

        val envelopeRecyclerView = findViewById<RecyclerView>(R.id.envelopeRecyclerView)

        //adapter
        envelopeRecyclerView.adapter = EnvelopeAdapter(EnvelopeTempMemory.envelopes)

        // Recalculate after returning from expenses / envelopes screens
        refreshHomeTotals()
    }

    // Sum expenses for the current calendar month and push the value into the home card
    private fun refreshHomeTotals() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        moneySpentThisMonth = ExpenseTempMemory.expensesInMonth(year, month)
            .sumOf { expense -> expense.amount }

        val totalSpentThisMonthText = findViewById<TextView>(R.id.totalSpentThisMonthText)
        totalSpentThisMonthText.text = ExpenseTempMemory.formatAmount(moneySpentThisMonth)
    }
}
