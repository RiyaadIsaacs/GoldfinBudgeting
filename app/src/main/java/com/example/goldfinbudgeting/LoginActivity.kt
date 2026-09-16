package com.example.goldfinbudgeting


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent

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
            val intent = Intent(this, CreateAccountActivity::class.java)

            startActivity(intent)
        }

        // Sign In
        // checks email and password then opens Home if they match
        signInButton.setOnClickListener {

            val email = emailEditText.text.toString().trim()

            val password = passwordEditText.text.toString().trim()

            // check if fields were filled before signing in
            if (email.isEmpty() || password.isEmpty())
            {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
            // check email and password against the Room users table
            // default demo account is still 1 and 1 until changed on profile
            else if (AccountStore.authenticate(this, email, password))
            {
                Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()

                // open the home screen after a successful login
                val intent = Intent(this, HomeActivity::class.java)

                startActivity(intent)

                finish()
            }
            else //when email or password is incorrect send message. dont go to next screen
            {
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
