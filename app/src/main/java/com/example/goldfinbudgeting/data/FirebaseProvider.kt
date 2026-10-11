package com.example.goldfinbudgeting.data

import android.os.Looper
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// Shared Firebase Auth
// Paths: users/{uid}/categories|expenses|monthlyGoals
object FirebaseProvider {
    private const val TAG = "FirebaseProvider"
    private const val TIMEOUT_SEC = 30L

    // use this pool when called from Activities
    private val awaitExecutor = Executors.newCachedThreadPool()

    fun auth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun firestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Root document for the signed-in user
    fun userDoc(uid: String): DocumentReference {
        return firestore().collection("users").document(uid)
    }

    fun categories(uid: String): CollectionReference {
        return userDoc(uid).collection("categories")
    }

    fun expenses(uid: String): CollectionReference {
        return userDoc(uid).collection("expenses")
    }

    fun monthlyGoals(uid: String): CollectionReference {
        return userDoc(uid).collection("monthlyGoals")
    }

    // Wait for a Firebase Task so store APIs can stay synchronous for Activities
    // If we are on the main thread, wait on a background worker
    fun <T> await(task: Task<T>): T {
        val onMainThread = Looper.myLooper() == Looper.getMainLooper()
        return if (onMainThread) {
            try {
                awaitExecutor.submit(Callable {
                    Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS)
                }).get(TIMEOUT_SEC + 5, TimeUnit.SECONDS)
            } catch (e: ExecutionException) {
                // Unwrap so AccountStore can catch FirebaseAuth*Exception types
                val cause = e.cause
                when (cause) {
                    is RuntimeException -> throw cause
                    is Exception -> throw cause
                    else -> throw e
                }
            }
        } else {
            Tasks.await(task, TIMEOUT_SEC, TimeUnit.SECONDS)
        }
    }

    fun logRead(collection: String, uid: String, count: Int) {
        Log.d(TAG, "Firestore read $collection uid=$uid count=$count")
    }

    fun logWrite(action: String, collection: String, uid: String, docId: String) {
        Log.d(TAG, "Firestore $action $collection uid=$uid id=$docId")
    }
}
