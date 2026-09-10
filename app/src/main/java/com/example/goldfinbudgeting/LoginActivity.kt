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

        val emailEditText = findViewById<EditText>(R.id.emailEditText)

        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        val signInButton = findViewById<Button>(R.id.signInButton)

        //listen for when user clicks button to sign in
        signInButton.setOnClickListener {

            val email = emailEditText.text.toString().trim()

            val password = passwordEditText.text.toString().trim()

            //check if field were filled before signing in and going to home screen
            if (email.isEmpty() || password.isEmpty())
            {
                //give message to fill in fields
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
            //this is the set email and password to signin
            else if (email == "1" && password == "1")
            {
                Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()

                //open and run home screen with correct email and password
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