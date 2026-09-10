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

class ExpensesActivity : AppCompatActivity() {
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

        //find the views we need by their id
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        val homeTab = findViewById<TextView>(R.id.homeTab)

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
    }
}