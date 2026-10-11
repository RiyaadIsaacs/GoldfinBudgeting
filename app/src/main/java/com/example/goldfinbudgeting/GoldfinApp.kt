package com.example.goldfinbudgeting

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

// GoldfinApp is registered in AndroidManifest as android:name=".GoldfinApp".
// It runs before any Activity so Firebase and the stores are ready for login / lists
class GoldfinApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("GoldfinApp", "Initialising Firebase")
        FirebaseApp.initializeApp(this)

        ExpenseTempMemory.bind(this)
        EnvelopeTempMemory.bind(this)
        GamificationStore.bind(this)
    }
}
