package com.example.goldfinbudgeting

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL

// Sends the chat to llama3 running in Ollama on this computer
object OllamaClient {

    //is runs the emulator, so every group member uses same address
    private const val API_URL = "http://127.0.0.1:11434/api/chat"

    private const val MODEL = "llama3"

    //system prompt that tells the ai what it needs to know in order to respond to the user
    private const val NAVIGATION_GUIDE = """
        You are an AI GoldFin assistant. You only help the user find screens and explain how to use the budgeting app. Do not give financial advice.
        The app includes the following:
        - Bottom bar navigation: Profile on the left, Home in the middle, Expenses on the right. Home shows Total Spent This Month and the envelope cards. Profile shows where the user can edit their display name and change their profile picture, mobile number, password and email address for logging into the app.
        - The "☰" icon opens the side menu which displays Envelopes, Accounts, Badges, and the AI Assistant
        - Envelopes lists spending envelopes the user can  filter by chosen month (envelopes are only displayed by month on this screen. it does not have a feature that displays all the envelopes despite the month). Edit Envelopes allows users to add one with a name, minimum goal, maximum goal, and date created.
        - Expenses has a search box, a month filterer, From date, To date, Clear, category chips, and monthly goals. Edit goals allows the user to set the month minimum and maximum. Edit Expenses allows the user to add an expense with an entry name, amount, date, start time, end time, description, category, and an optional receipt photo. View spending graph opens the Spending screen.
        - Spending shows gold for money spent, a black mark for the minimum goal, and a red mark for the maximum goal.
        - Badges shows the XP level, a daily goal to add 1 expense entry, a weekly goal to add 5 expenses entries, and badge tiles. A red dot will show on Badges in the side menu when a badge was achieved but not viewed yet.
        - Log Out is at the top right and returns to the login screen. 
        - On the login screen the user can click New User to create an new account.

        Rules: Answer in 3 sentences or less. Name the exact button or tab to tap and where to find it. If the question is not about using GoldFin, say you only help with navigating the GoldFin app.
    """

    private val mainHandler = Handler(Looper.getMainLooper())

    //ask llama3 in background thread. Then return a reply on screen thread
    fun ask(history: List<Message>, onReply: (String) -> Unit) {

        //copy the chat so a new message cannot change this request
        val snapshot = history.toList()

        Thread {

            val reply = try {

                requestReply(snapshot)

            } catch (e: Exception) {

                Log.e("OllamaClient", "Chat request failed", e)

                //if app is not connected to ollama show this in speech bubble
                "Could not connect to the assistant. Make sure Ollama is running on this computer."
            }

            mainHandler.post {

                onReply(reply)
            }

        }.start()
    }

    //post chat to ollama and read assistant text
    private fun requestReply(history: List<Message>): String {

        val messages = JSONArray()

        messages.put(
            JSONObject()

                .put("role", "system")

                .put("content", NAVIGATION_GUIDE.trim())
        )

        //keep questions and replies on screen
        for (message in history) {

            if (message.text == "Typing...") continue

            val role = if (message.isFromUser) "user" else "assistant"

            messages.put(
                JSONObject()

                    .put("role", role)

                    .put("content", message.text)
            )
        }

        val body = JSONObject()
            .put("model", MODEL)
            .put("messages", messages)
            .put("stream", false)

        val bytes = body.toString().toByteArray(Charsets.UTF_8)

        val connection = URL(API_URL).openConnection(Proxy.NO_PROXY) as HttpURLConnection

        try {
            connection.requestMethod = "POST"

            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            connection.connectTimeout = 15000
            connection.readTimeout = 120000

            connection.doOutput = true

            connection.setRequestProperty("Connection", "close")

            //send the whole question in one go, then read the reply
            val output = connection.outputStream

            output.write(bytes)

            output.close()

            val responseCode = connection.responseCode

            val responseText = if (responseCode == HttpURLConnection.HTTP_OK) {

                connection.inputStream.bufferedReader().use { it.readText() }

            } else {

                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()

                return "The assistant could not answer just now ($responseCode). $errorText"
            }

            val content = JSONObject(responseText)
                .getJSONObject("message")
                .getString("content")

                .trim()

            if (content.isEmpty()) {

                return "The assistant did not send a reply."
            }

            return content

        } finally {

            connection.disconnect()
        }
    }
}
