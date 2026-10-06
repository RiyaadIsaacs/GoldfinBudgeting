package com.example.goldfinbudgeting

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

// Which screen is currently open for nav highlighting
enum class NavScreen {
    HOME,
    PROFILE,
    EXPENSES,
    ENVELOPES,
    BADGES,
    NONE
}

// Keeps bottom tabs and drawer active styles consistent across screens
// Default bar is the expenses gold strip. Active tab and drawer item use the stronger gradient
object NavHighlight {

    fun applyBottomTabs(activity: Activity, active: NavScreen) {
        val profileTab = activity.findViewById<TextView?>(R.id.profileTab)
        val homeTab = activity.findViewById<TextView?>(R.id.homeTab)
        val expensesTab = activity.findViewById<TextView?>(R.id.expensesTab)

        profileTab?.let { clearTab(it) }
        homeTab?.let { clearTab(it) }
        expensesTab?.let { clearTab(it) }

        when (active) {
            NavScreen.PROFILE -> profileTab?.let { highlightTab(it) }
            NavScreen.HOME -> homeTab?.let { highlightTab(it) }
            NavScreen.EXPENSES -> expensesTab?.let { highlightTab(it) }
            else -> Unit
        }
    }

    fun applyDrawer(activity: Activity, active: NavScreen) {
        val envelopes = activity.findViewById<TextView?>(R.id.envelopesMenuItem)
        val badges = activity.findViewById<TextView?>(R.id.badgesMenuItem)

        envelopes?.let { clearDrawerItem(it) }
        badges?.let { clearDrawerItem(it) }

        when (active) {
            NavScreen.ENVELOPES -> envelopes?.let { highlightDrawerItem(it) }
            NavScreen.BADGES -> badges?.let { highlightDrawerItem(it) }
            else -> Unit
        }

        updateBadgesDot(activity)
    }

    fun apply(activity: Activity, bottom: NavScreen, drawer: NavScreen = NavScreen.NONE) {
        applyBottomTabs(activity, bottom)
        applyDrawer(activity, drawer)
    }

    // Bottom tabs jump straight to that screen. Home stays underneath, so
    // Home, Expenses, then Profile, then Home does not stop on Expenses.
    // Profile is the left tab, so it enters from the left and leaves to the left.
    fun openBottomTab(activity: Activity, screen: NavScreen) {
        val destination = when (screen) {
            NavScreen.HOME -> HomeActivity::class.java
            NavScreen.PROFILE -> ProfileActivity::class.java
            NavScreen.EXPENSES -> ExpensesActivity::class.java
            else -> return
        }

        if (activity.javaClass == destination) return

        val intent = Intent(activity, destination)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        when {
            screen == NavScreen.PROFILE -> {
                startWithSlide(activity, intent, R.anim.slide_in_left, R.anim.slide_out_right)
            }
            activity is ProfileActivity -> {
                startWithSlide(activity, intent, R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else -> activity.startActivity(intent)
        }
    }

    // enterAnim is the screen coming in. exitAnim is the screen leaving.
    private fun startWithSlide(activity: Activity, intent: Intent, enterAnim: Int, exitAnim: Int) {
        val options = ActivityOptions.makeCustomAnimation(activity, enterAnim, exitAnim)
        activity.startActivity(intent, options.toBundle())
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(enterAnim, exitAnim)
    }

    fun updateBadgesDot(activity: Activity) {
        val dot = activity.findViewById<View?>(R.id.badgesNotificationDot) ?: return
        dot.visibility = if (GamificationStore.hasUnseenBadges()) View.VISIBLE else View.GONE
    }

    private fun highlightTab(tab: TextView) {
        tab.background = ContextCompat.getDrawable(tab.context, R.drawable.nav_tab_active)?.mutate()
        tab.backgroundTintList = null
        tab.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        tab.setTextColor(Color.BLACK)
    }

    private fun clearTab(tab: TextView) {
        tab.background = null
        tab.backgroundTintList = null
        tab.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        tab.setTextColor(Color.BLACK)
    }

    private fun highlightDrawerItem(item: TextView) {
        val gradient = ContextCompat.getDrawable(item.context, R.drawable.nav_tab_active)?.mutate()

        // badges and envelopes sit inside a FrameLayout so paint the row for a full gradient
        val row = item.parent as? FrameLayout
        if (row != null) {
            item.setBackgroundColor(Color.TRANSPARENT)
            item.backgroundTintList = null
            row.background = gradient
            row.backgroundTintList = null
        } else {
            item.background = gradient
            item.backgroundTintList = null
        }

        item.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        item.setTextColor(Color.BLACK)
    }

    private fun clearDrawerItem(item: TextView) {
        val row = item.parent as? FrameLayout

        if (row != null) {
            row.setBackgroundColor(0xFFE6DDF5.toInt())
            row.backgroundTintList = null
            item.setBackgroundColor(Color.TRANSPARENT)
            item.backgroundTintList = null
        } else {
            item.setBackgroundColor(0xFFE6DDF5.toInt())
            item.backgroundTintList = null
        }

        item.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        item.setTextColor(0xFF333333.toInt())
    }
}
