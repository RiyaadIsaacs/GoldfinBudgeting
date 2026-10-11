package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        //find edit text field for email
        val emailEditText = findViewById<EditText>(R.id.emailEditText)

        //find edit text field for password
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        //find sign in button
        val signInButton = findViewById<Button>(R.id.signInButton)

        //find New User button (opens the create account screen)
        val newUserButton = findViewById<Button>(R.id.newUserButton)

        // New User
        // opens the create account page
        newUserButton.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        // Sign In
        // checks email and password then opens Home if they match
        signInButton.setOnClickListener {

            val email = emailEditText.text.toString().trim()

            val password = passwordEditText.text.toString().trim()

            // check if fields were filled before signing in
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            } else {
                signInButton.isEnabled = false
                Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()

                AccountStore.authenticate(this, email, password) { success ->
                    signInButton.isEnabled = true
                    if (success) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
