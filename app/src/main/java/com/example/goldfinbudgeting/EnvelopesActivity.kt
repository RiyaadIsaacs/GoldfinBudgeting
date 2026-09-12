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

class EnvelopesActivity : AppCompatActivity() {
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




        //open edit envelopes screen
        editEnvelopesButton.setOnClickListener {
            val intent = Intent(this, EditEnvelopesActivity::class.java)

            startActivity(intent)
        }
    }
}