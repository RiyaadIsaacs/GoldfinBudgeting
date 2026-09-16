package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.DatabaseProvider
import com.example.goldfinbudgeting.data.UserEntity

// AccountStore helpers for login accounts in Room
// Used by Login Create Account and Profile
object AccountStore {
    private const val TAG = "AccountStore"

    // shortcut to the users table
    private fun userDao(context: Context) = DatabaseProvider.get(context).userDao()

    // Current account
    // finds which user is signed in or falls back to demo user 1
    private fun currentUser(context: Context): UserEntity {
        val dao = userDao(context)
        val remembered = prefsUsername(context)

        // prefer the email saved after the last successful login
        if (remembered != null) {
            dao.findByUsername(remembered)?.let { return it }
        }

        // otherwise use the demo account
        dao.findByUsername("1")?.let { return it }

        // last resort create the demo account if the table somehow has no user 1
        val id = dao.insert(UserEntity(username = "1", password = "1"))
        Log.d(TAG, "Created fallback user id=$id")
        return dao.findByUsername("1") ?: UserEntity(id = id, username = "1", password = "1")
    }

    // email of the current account
    fun email(context: Context): String {
        return currentUser(context).username
    }

    // Room id of the current account so each users data stays separate
    fun currentUserId(context: Context): Long {
        return currentUser(context).id
    }

    // true only for the demo login 1 which keeps the sample data
    fun isDemoAccount(context: Context): Boolean {
        return email(context) == "1"
    }

    // password of the current account
    fun password(context: Context): String {
        return currentUser(context).password
    }

    // Profile updates
    // change the login email from the profile screen
    fun setEmail(context: Context, email: String) {
        val trimmed = email.trim()
        val dao = userDao(context)
        val current = currentUser(context)

        dao.update(current.copy(username = trimmed))

        // keep local memory of who is signed in
        rememberUsername(context, trimmed)
        Log.d(TAG, "Updated username to $trimmed for user id=${current.id}")
    }

    // change the login password from the profile Change Password popups
    fun setPassword(context: Context, password: String) {
        val user = currentUser(context)

        userDao(context).update(user.copy(password = password))

        Log.d(TAG, "Updated password for user id=${user.id}")
    }

    // Login
    // returns true if email and password match a row in the users table
    fun authenticate(context: Context, username: String, password: String): Boolean {
        val match = userDao(context).authenticate(username.trim(), password)

        if (match != null) {
            // remember this user so envelopes and expenses load for their account
            rememberUsername(context, match.username)

            Log.d(TAG, "Login success for ${match.username}")

            return true
        }

        Log.d(TAG, "Login failed for $username")

        return false
    }

    // Create account
    // inserts a new user. returns false if that email is already taken
    // new accounts start with empty envelopes and expenses
    fun createAccount(context: Context, username: String, password: String): Boolean {
        val trimmed = username.trim()
        val dao = userDao(context)

        // stop if someone already registered with this email
        if (dao.findByUsername(trimmed) != null) {
            Log.d(TAG, "Create account failed. username already exists: $trimmed")
            return false
        }

        val id = dao.insert(UserEntity(username = trimmed, password = password))

        Log.d(TAG, "Created new user id=$id username=$trimmed")

        return true
    }

    // SharedPreferences helpers
    // small local file that remembers which email is active after login
    private fun prefs(context: Context) = context.getSharedPreferences("goldfin_profile", Context.MODE_PRIVATE)

    private fun prefsUsername(context: Context): String? {
        return prefs(context).getString("login_username", null)
    }

    private fun rememberUsername(context: Context, username: String) {
        prefs(context).edit().putString("login_username", username).apply()
    }
}
