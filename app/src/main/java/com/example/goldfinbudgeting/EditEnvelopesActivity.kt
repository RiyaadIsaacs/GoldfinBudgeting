package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout

class EditEnvelopesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_edit_envelopes)

        //keep content from hiding behind status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }

        //find drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        //find burger menu icon
        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        //find close drawer button
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find log out button
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find home buttn
        val homeTab = findViewById<TextView>(R.id.homeTab)

        //find expenses button
        val expensesTab = findViewById<TextView>(R.id.expensesTab)

        //find envelopes drawer button
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)

        //find cancel button
        val cancelButton = findViewById<TextView>(R.id.cancelButton)

        //find add button
        val addButton = findViewById<TextView>(R.id.addButton)

        //find edit text
        val envelopeNameEditText = findViewById<EditText>(R.id.envelopeNameEditText)

        //open burger menu
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //close burger menu
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //go to login screen
        logOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }

        //close current active screen and go hoome
        homeTab.setOnClickListener {
            finish()
        }

        //open expenses screen
        expensesTab.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)

            startActivity(intent)
        }

        //open envelopes screen
        envelopesMenuItem.setOnClickListener {
            val intent = Intent(this, EnvelopesActivity::class.java)

            startActivity(intent)
        }

        // Discard and go back
        cancelButton.setOnClickListener {
            finish()
        }

        //placeholder until saving is added. these is where we will add the local database storage
        addButton.setOnClickListener {
            val name = envelopeNameEditText.text.toString()

            Toast.makeText(this, "Would add: $name", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}