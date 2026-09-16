package com.example.goldfinbudgeting

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Create Account screen
class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // load the create account layout
        setContentView(R.layout.activity_create_account)

        // fields and buttons
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val backToLoginButton = findViewById<Button>(R.id.backToLoginButton)

        // Create Account
        // checks the fields then saves the new login if everything is valid
        createAccountButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // all three fields must be filled in
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            // password and confirm password must match
            else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else {
                // try to insert the new user into the Room users table
                val created = AccountStore.createAccount(this, email, password)

                if (created) {
                    // go back to login so they can sign in with the new details
                    Toast.makeText(this, "Account created. Please sign in", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // email is already taken
                    Toast.makeText(this, "That email is already in use", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Back
        // closes this screen and returns to login without saving
        backToLoginButton.setOnClickListener {
            finish()
        }
    }
}
