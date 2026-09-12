package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_profile)

        //keep content from hiding behind status bar like the other screens
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            insets
        }

        //find drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        //find hamburger menu icon
        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        //find close button
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find log out button
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find home tab button
        val homeTab = findViewById<TextView>(R.id.homeTab)

        //find expenses tab button
        val expensesTab = findViewById<TextView>(R.id.expensesTab)

        //find envelopes in the drawer
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)

        //profile form fields
        val displayNameHeading = findViewById<TextView>(R.id.displayNameHeading)
        val displayNameEditText = findViewById<EditText>(R.id.displayNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val mobileEditText = findViewById<EditText>(R.id.mobileEditText)

        //load any saved profile values
        val profilePrefs = getSharedPreferences("goldfin_profile", MODE_PRIVATE)

        displayNameEditText.setText(profilePrefs.getString("display_name", ""))
        emailEditText.setText(profilePrefs.getString("email", ""))
        mobileEditText.setText(profilePrefs.getString("mobile", ""))

        val savedName = profilePrefs.getString("display_name", "")
        if (!savedName.isNullOrBlank()) {
            displayNameHeading.text = savedName
        }

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

        //close profile screen and go back to home
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

        //save display name and update the heading
        findViewById<TextView>(R.id.saveDisplayNameButton).setOnClickListener {
            val name = displayNameEditText.text.toString().trim()

            profilePrefs.edit().putString("display_name", name).apply()

            displayNameHeading.text = if (name.isEmpty()) "MyName" else name

            Toast.makeText(this, "Display name saved", Toast.LENGTH_SHORT).show()
        }

        //save email address
        findViewById<TextView>(R.id.saveEmailButton).setOnClickListener {
            profilePrefs.edit().putString("email", emailEditText.text.toString().trim()).apply()

            Toast.makeText(this, "Email saved", Toast.LENGTH_SHORT).show()
        }

        //save mobile number
        findViewById<TextView>(R.id.saveMobileButton).setOnClickListener {
            profilePrefs.edit().putString("mobile", mobileEditText.text.toString().trim()).apply()

            Toast.makeText(this, "Mobile number saved", Toast.LENGTH_SHORT).show()
        }

        //security policy placeholder until that page is built
        findViewById<LinearLayout>(R.id.securityPolicyButton).setOnClickListener {
            Toast.makeText(this, "Security Policy coming soon", Toast.LENGTH_SHORT).show()
        }

        //account settings placeholder until that page is built
        findViewById<LinearLayout>(R.id.accountSettingsButton).setOnClickListener {
            Toast.makeText(this, "Account Settings coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
