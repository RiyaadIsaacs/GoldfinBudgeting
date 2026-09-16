package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.DatabaseProvider
import com.example.goldfinbudgeting.data.UserEntity

// AccountStore handles login username / password 
// Profile display fields (name, mobile, avatar)
object AccountStore {
    private const val TAG = "AccountStore"

    // Shortcut to the UserDao from the shared Room database
    private fun userDao(context: Context) = DatabaseProvider.get(context).userDao()

    // Finds the account we should treat as the current user
    private fun currentUser(context: Context): UserEntity {
        val dao = userDao(context)
        val remembered = prefsUsername(context)

        if (remembered != null) {
            dao.findByUsername(remembered)?.let { return it }
        }

        dao.findByUsername("1")?.let { return it }

        // Should keep login safe
        val id = dao.insert(UserEntity(username = "1", password = "1"))
        Log.d(TAG, "Created fallback user id=$id")
        return dao.findByUsername("1") ?: UserEntity(id = id, username = "1", password = "1")
    }

    // Returns the current Room username
    fun email(context: Context): String {
        return currentUser(context).username
    }

    // Returns the current Room password for the remembered / default user
    fun password(context: Context): String {
        return currentUser(context).password
    }

    // Updates the username in Room when the profile's email field is saved
    fun setEmail(context: Context, email: String) {
        val trimmed = email.trim()
        val dao = userDao(context)
        val current = currentUser(context)

        dao.update(current.copy(username = trimmed))

        // Remember which username to load next time
        rememberUsername(context, trimmed)
        Log.d(TAG, "Updated username to $trimmed for user id=${current.id}")
    }

    // Updates the password in Room when the user changes it on the profile screen
    fun setPassword(context: Context, password: String) {
        val user = currentUser(context)

        userDao(context).update(user.copy(password = password))

        Log.d(TAG, "Updated password for user id=${user.id}")
    }

    // Login check used by LoginActivity 
    fun authenticate(context: Context, username: String, password: String): Boolean {
        val match = userDao(context).authenticate(username.trim(), password)

        if (match != null) {
            rememberUsername(context, match.username)

            Log.d(TAG, "Login success for ${match.username}")

            return true
        }

        Log.d(TAG, "Login failed for $username")

        return false
    }

    // SharedPreferences file also used by ProfileActivity for non-login profile fields
    private fun prefs(context: Context) = context.getSharedPreferences("goldfin_profile", Context.MODE_PRIVATE)

    // Remembered username so we know which Room user is active after login
    private fun prefsUsername(context: Context): String? { return prefs(context).getString("login_username", null)}

    // Saves the active username locally
    private fun rememberUsername(context: Context, username: String) {prefs(context).edit().putString("login_username", username).apply()}
}
