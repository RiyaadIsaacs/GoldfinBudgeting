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

        //find AI assistant
        val aiAssistantMenuItem = findViewById<TextView>(R.id.aiAssistantMenuItem)

        //open AI assistant screen
        aiAssistantMenuItem.setOnClickListener {
            val intent = Intent(this, AIAssistantActivity::class.java)

            startActivity(intent)
        }

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
            NavHighlight.openBottomTab(this, NavScreen.EXPENSES)
        }

        //open profile screen from the right
        profileTab.setOnClickListener {
            NavHighlight.openBottomTab(this, NavScreen.PROFILE)
        }

        //find envelopes in the drawer
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)
        val badgesMenuItem = findViewById<TextView>(R.id.badgesMenuItem)

        //open envelopes screen
        envelopesMenuItem.setOnClickListener {
            val intent = Intent(this, EnvelopesActivity::class.java)

            startActivity(intent)
        }

        //open badges screen
        badgesMenuItem.setOnClickListener {
            startActivity(Intent(this, BadgesActivity::class.java))
        }

        NavHighlight.apply(this, bottom = NavScreen.HOME, drawer = NavScreen.NONE)
        maybeShowDailyTaskPrompt()
    }

    //refresh envelope list and monthly total when screen is visible again
    override fun onResume() {
        super.onResume()

        NavHighlight.apply(this, bottom = NavScreen.HOME, drawer = NavScreen.NONE)

        val envelopeRecyclerView = findViewById<RecyclerView>(R.id.envelopeRecyclerView)

        //adapter
        envelopeRecyclerView.adapter = EnvelopeAdapter(EnvelopeTempMemory.envelopes)

        // Recalculate after returning from expenses / envelopes screens
        refreshHomeTotals()
    }

    // remind them of the daily task once a day if it is not done yet
    private fun maybeShowDailyTaskPrompt() {
        if (!GamificationStore.shouldShowDailyPrompt()) {
            return
        }

        GamificationStore.markDailyPromptShown()

        val dialogView = layoutInflater.inflate(R.layout.dialog_daily_task, null)
        val messageText = dialogView.findViewById<TextView>(R.id.dailyTaskMessageText)
        val gotItButton = dialogView.findViewById<TextView>(R.id.dailyTaskGotItButton)

        messageText.text = "Your daily task: ${GamificationStore.dailyGoalLabel()}"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        gotItButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
