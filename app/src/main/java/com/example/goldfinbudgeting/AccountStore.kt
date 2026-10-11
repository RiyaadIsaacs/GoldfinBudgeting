package com.example.goldfinbudgeting

import android.content.Context
import android.util.Log
import com.example.goldfinbudgeting.data.FirebaseProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

// AccountStore helpers for login via Firebase Authentication (email / password)
object AccountStore {
    private const val TAG = "AccountStore"

    // Optional demo email
    private const val DEMO_EMAIL = "demo@goldfin.app"

    // Last password successfully verified in the profile flow (needed for Auth re-authenticate)
    @Volatile
    private var verifiedPassword: String? = null

    private fun auth() = FirebaseProvider.auth()

    // Email of the signed-in Firebase user (empty if signed out)
    fun email(context: Context): String {
        return auth().currentUser?.email
            ?: prefsUsername(context).orEmpty()
    }

    // Firebase Auth UID so each user's Firestore data stays separate
    fun currentUserId(context: Context): String {
        return auth().currentUser?.uid.orEmpty()
    }

    // only for the optional demo@goldfin.app account
    fun isDemoAccount(context: Context): Boolean {
        return email(context).equals(DEMO_EMAIL, ignoreCase = true)
    }

    // Firebase Auth does not allow reading the stored password
    fun password(context: Context): String {
        return verifiedPassword.orEmpty()
    }

    // Re-authenticate with the typed current password
    fun verifyPassword(context: Context, password: String, onComplete: (Boolean) -> Unit) {
        val user = auth().currentUser
        val mail = user?.email
        if (user == null || mail == null) {
            onComplete(false)
            return
        }
        val credential = EmailAuthProvider.getCredential(mail, password)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                verifiedPassword = password
                Log.d(TAG, "Password verified for $mail")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Password verify failed: ${e.message}")
                onComplete(false)
            }
    }

    // Change the login email from the profile screen
    fun setEmail(context: Context, email: String, onComplete: (Boolean) -> Unit = {}) {
        val trimmed = email.trim()
        val user = auth().currentUser
        if (user == null) {
            onComplete(false)
            return
        }
        user.updateEmail(trimmed)
            .addOnSuccessListener {
                rememberUsername(context, trimmed)
                Log.d(TAG, "Updated email to $trimmed for uid=${user.uid}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update email: ${e.message}")
                onComplete(false)
            }
    }

    // Change the login password after verifyPassword() succeeded in Profile
    fun setPassword(context: Context, password: String, onComplete: (Boolean) -> Unit = {}) {
        val user = auth().currentUser
        if (user == null) {
            onComplete(false)
            return
        }
        user.updatePassword(password)
            .addOnSuccessListener {
                verifiedPassword = password
                Log.d(TAG, "Updated password for uid=${user.uid}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update password: ${e.message}")
                onComplete(false)
            }
    }

    // Login — onComplete(true) if Firebase Auth accepts the email and password
    fun authenticate(
        context: Context,
        username: String,
        password: String,
        onComplete: (Boolean) -> Unit
    ) {
        val email = username.trim()
        auth().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                verifiedPassword = password
                rememberUsername(context, email)
                Log.d(TAG, "Login success for $email uid=${auth().currentUser?.uid}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                when (e) {
                    is FirebaseAuthInvalidUserException ->
                        Log.d(TAG, "Login failed (unknown user): $email")
                    is FirebaseAuthInvalidCredentialsException ->
                        Log.d(TAG, "Login failed (bad credentials): $email")
                    else ->
                        Log.d(TAG, "Login failed for $email: ${e.message}")
                }
                onComplete(false)
            }
    }

    // Create account — onComplete(null) on success, or an error message on failure
    fun createAccount(
        context: Context,
        username: String,
        password: String,
        onComplete: (String?) -> Unit
    ) {
        val email = username.trim()
        if (!email.contains("@")) {
            onComplete("Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            onComplete("Password must be at least 6 characters")
            return
        }

        auth().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Sign out so they log in explicitly from the login screen
                auth().signOut()
                verifiedPassword = null
                Log.d(TAG, "Created new Auth user email=$email")
                onComplete(null)
            }
            .addOnFailureListener { e ->
                val message = when (e) {
                    is FirebaseAuthUserCollisionException -> {
                        Log.d(TAG, "Create account failed. email already exists: $email")
                        "That email is already in use. Go back and sign in instead."
                    }
                    is FirebaseAuthWeakPasswordException -> {
                        Log.d(TAG, "Create account failed. weak password for $email")
                        "Password must be at least 6 characters"
                    }
                    else -> {
                        Log.d(TAG, "Create account failed for $email: ${e.message}")
                        "Could not create account: ${e.message ?: "unknown error"}"
                    }
                }
                onComplete(message)
            }
    }

    fun signOut(context: Context) {
        auth().signOut()
        verifiedPassword = null
        prefs(context).edit().remove("login_username").apply()
        Log.d(TAG, "Signed out")
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("goldfin_profile", Context.MODE_PRIVATE)

    private fun prefsUsername(context: Context): String? {
        return prefs(context).getString("login_username", null)
    }

    private fun rememberUsername(context: Context, username: String) {
        prefs(context).edit().putString("login_username", username).apply()
    }
}
