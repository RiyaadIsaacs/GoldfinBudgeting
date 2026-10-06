package com.example.goldfinbudgeting

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AIAssistantActivity : AppCompatActivity() {

    //holds chat messages for current screen view. default message displayed
    private val messages = mutableListOf(

        Message("Hi! I'm your AI app assistant. How can I help you navigate GoldFin today?", false)
    )

    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_assistant)

        //keeps content from hiding behind status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //find drawer layout via their id
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        //find menu icon via their id
        val menuIcon = findViewById<TextView>(R.id.menuIcon)

        //find close drawer button via their id
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)

        //find logout button via their id
        val logOutButton = findViewById<TextView>(R.id.logOutButton)

        //find envelop menu itme via their id
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)

        //find message recycler via their id
        val messageRecyclerView = findViewById<RecyclerView>(R.id.messageRecyclerView)

        //find message edit text via their id
        val messageEditText = findViewById<EditText>(R.id.messageEditText)

        //find send button via their id
        val sendButton = findViewById<TextView>(R.id.sendButton)

        //chat list set up
        adapter = MessageAdapter(messages)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = adapter

        //side menu opener
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //close side menu
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        //back to login screen
        logOutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }

        //open envelope screen
        envelopesMenuItem.setOnClickListener {
            val intent = Intent(this, EnvelopesActivity::class.java)

            startActivity(intent)
        }

        //send message when button tapped
        sendButton.setOnClickListener {

            val text = messageEditText.text.toString()

            if (text.isNotBlank()) {
                messages.add(Message(text, true))

                adapter.notifyItemInserted(messages.size - 1)

                messageRecyclerView.scrollToPosition(messages.size - 1)

                messageEditText.text.clear()

                //placeholder reply until ollam is connected
                messages.add(Message("AI assistant in development.", false))

                adapter.notifyItemInserted(messages.size - 1)

                messageRecyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }
}