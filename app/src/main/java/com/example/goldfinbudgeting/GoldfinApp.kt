package com.example.goldfinbudgeting

import android.app.Application
import android.util.Log
import com.example.goldfinbudgeting.data.DatabaseProvider

// GoldfinApp is registered in AndroidManifest as android:name=".GoldfinApp"
// It runs before any Activity so Room and the stores are ready for login / lists
class GoldfinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Open (or create) the local Room database and seed demo data if needed
        Log.d("GoldfinApp", "Initialising Room database")
        DatabaseProvider.get(this)
        // Give the expense / envelope stores an Application context so they can reach Room
        ExpenseTempMemory.bind(this)
        EnvelopeTempMemory.bind(this)
    }
}
