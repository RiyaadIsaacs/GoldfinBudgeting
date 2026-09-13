package com.example.goldfinbudgeting

import android.content.Context

//login email and password. starts as 1 / 1 for testing
object AccountStore {
    private fun prefs(context: Context) = context.getSharedPreferences("goldfin_profile", Context.MODE_PRIVATE)

    fun email(context: Context): String {
        return prefs(context).getString("login_email", "1") ?: "1"
    }

    fun password(context: Context): String {
        return prefs(context).getString("login_password", "1") ?: "1"
    }

    fun setEmail(context: Context, email: String) {
        prefs(context).edit().putString("login_email", email).apply()
    }

    fun setPassword(context: Context, password: String) {
        prefs(context).edit().putString("login_password", password).apply()
    }
}
