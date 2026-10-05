package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class BadgesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        val drawer = findViewById<DrawerLayout>(R.id.badgesDrawerLayout)
        findViewById<TextView>(R.id.badgesMenuIcon).setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.closeBadgesDrawerButton).setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.badgesEnvelopesMenuItem).setOnClickListener {
            startActivity(Intent(this, EnvelopesActivity::class.java))
        }

        findViewById<TextView>(R.id.badgesHomeTab).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        findViewById<TextView>(R.id.badgesProfileTab).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<TextView>(R.id.badgesExpensesTab).setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java))
        }
    }
}
