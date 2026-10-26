package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val loginScroll = findViewById<ScrollView>(R.id.loginScroll)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val newUserButton = findViewById<Button>(R.id.newUserButton)

        // Pad for keyboard / system bars and scroll the focused field into view.
        SoftInputHelper.setupFormScreen(this, loginScroll, emailEditText, passwordEditText)

        newUserButton.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            } else {
                signInButton.isEnabled = false
                Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()

                AccountStore.authenticate(this, email, password) { error ->
                    signInButton.isEnabled = true
                    if (error == null) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
