package com.example.goldfinbudgeting

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        //find profile button
        val profileTab = findViewById<TextView>(R.id.profileTab)

        //find envelopes drawer button
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)

        //find cancel button
        val cancelButton = findViewById<TextView>(R.id.cancelButton)

        //find add button
        val addButton = findViewById<TextView>(R.id.addButton)

        //find edit text
        val envelopeNameEditText = findViewById<EditText>(R.id.envelopeNameEditText)

        //find min goal edit text
        val minGoalEditText = findViewById<EditText>(R.id.minGoalEditText)

        //find max goal edit text
        val maxGoalEditText = findViewById<EditText>(R.id.maxGoalEditText)

        //find date created field
        val dateCreatedEditText = findViewById<EditText>(R.id.dateCreatedEditText)

        //find envelope list under the form
        val envelopeRecyclerView = findViewById<RecyclerView>(R.id.envelopeRecyclerView)

        //show the same cards as home and envelopes, with Delete like the hardcoded ones
        envelopeRecyclerView.layoutManager = LinearLayoutManager(this)
        refreshEnvelopeList(envelopeRecyclerView)

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

        //open profile screen
        profileTab.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

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

        //open a calendar when they tap the date field
        dateCreatedEditText.setOnClickListener {
            val calendar = Calendar.getInstance()

            calendar.timeInMillis = EnvelopeTempMemory.parseDate(dateCreatedEditText.text.toString())

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dateCreatedEditText.setText(EnvelopeTempMemory.formatDate(EnvelopeTempMemory.dateOn(year, month, day)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dateCreatedEditText.isFocusable = false
        dateCreatedEditText.isClickable = true

        //ask first, then add from the confirm page
        addButton.setOnClickListener {
            val name = envelopeNameEditText.text.toString().trim()

            val min = minGoalEditText.text.toString().toDoubleOrNull() ?: 0.0

            val max = maxGoalEditText.text.toString().toDoubleOrNull() ?: 0.0

            val dateCreated = EnvelopeTempMemory.parseDate(dateCreatedEditText.text.toString())

            if (name.isBlank()) {
                Toast.makeText(this, "Please enter an envelope name", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            val intent = Intent(this, ConfirmEnvelopeActivity::class.java)

            intent.putExtra("action", "add")
            intent.putExtra("name", name)
            intent.putExtra("min", min)
            intent.putExtra("max", max)
            intent.putExtra("dateCreated", dateCreated)

            startActivity(intent)
        }
    }

    //refresh list after coming back from the confirm page
    override fun onResume() {
        super.onResume()

        val envelopeRecyclerView = findViewById<RecyclerView>(R.id.envelopeRecyclerView)

        refreshEnvelopeList(envelopeRecyclerView)
    }

    private fun refreshEnvelopeList(envelopeRecyclerView: RecyclerView) {
        envelopeRecyclerView.adapter = EnvelopeAdapter(
            EnvelopeTempMemory.envelopes,
            showDelete = true
        ) { envelope ->
            val intent = Intent(this, ConfirmEnvelopeActivity::class.java)

            intent.putExtra("action", "delete")
            intent.putExtra("name", envelope.name)
            intent.putExtra("index", EnvelopeTempMemory.envelopes.indexOf(envelope))

            startActivity(intent)
        }
    }
}