package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConfirmEnvelopeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_confirm_envelope)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(systemBars.left + 24, systemBars.top, systemBars.right + 24, systemBars.bottom)

            insets
        }

        val action = intent.getStringExtra("action") ?: "add"
        val name = intent.getStringExtra("name") ?: ""
        val min = intent.getDoubleExtra("min", 0.0)
        val max = intent.getDoubleExtra("max", 0.0)
        val index = intent.getIntExtra("index", -1)

        val confirmMessage = findViewById<TextView>(R.id.confirmMessage)
        val confirmEnvelopeName = findViewById<TextView>(R.id.confirmEnvelopeName)
        val noButton = findViewById<TextView>(R.id.noButton)
        val yesButton = findViewById<TextView>(R.id.yesButton)

        if (action == "delete") {
            confirmMessage.text = "Are you sure you wish to delete this envelope?"
        } else {
            confirmMessage.text = "Are you sure you wish to add this envelope?"
        }

        confirmEnvelopeName.text = name

        //go back without changing anything
        noButton.setOnClickListener {
            finish()
        }

        //do the add or delete after they say yes
        yesButton.setOnClickListener {
            if (action == "delete") {
                if (index >= 0 && index < EnvelopeTempMemory.envelopes.size) {
                    val envelope = EnvelopeTempMemory.envelopes[index]

                    EnvelopeTempMemory.deleteEnvelope(envelope)

                    Toast.makeText(this, "Deleted ${envelope.name}", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (name.isNotBlank()) {
                    EnvelopeTempMemory.envelopes.add(Envelope(name, min, max, 0.0))

                    Toast.makeText(this, "Added $name", Toast.LENGTH_SHORT).show()
                }

                //after adding, go back to the main envelopes screen instead of the edit form
                val intent = Intent(this, EnvelopesActivity::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

                startActivity(intent)
            }

            finish()
        }
    }
}
