package com.example.goldfinbudgeting

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout

// Badges and XP screen for light gamification
class BadgesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_badges)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val menuIcon = findViewById<TextView>(R.id.menuIcon)
        val closeDrawerButton = findViewById<TextView>(R.id.closeDrawerButton)
        val logOutButton = findViewById<TextView>(R.id.logOutButton)
        val homeTab = findViewById<TextView>(R.id.homeTab)
        val expensesTab = findViewById<TextView>(R.id.expensesTab)
        val profileTab = findViewById<TextView>(R.id.profileTab)
        val envelopesMenuItem = findViewById<TextView>(R.id.envelopesMenuItem)
        val badgesMenuItem = findViewById<TextView>(R.id.badgesMenuItem)

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        badgesMenuItem.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        logOutButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        homeTab.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        expensesTab.setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java))
        }

        profileTab.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        envelopesMenuItem.setOnClickListener {
            startActivity(Intent(this, EnvelopesActivity::class.java))
        }

        NavHighlight.apply(this, bottom = NavScreen.NONE, drawer = NavScreen.BADGES)
        refreshBadgesScreen()
    }

    override fun onResume() {
        super.onResume()
        NavHighlight.apply(this, bottom = NavScreen.NONE, drawer = NavScreen.BADGES)
        refreshBadgesScreen()
    }

    // after they leave badges the drawer red dot can clear
    override fun onPause() {
        super.onPause()
        GamificationStore.markAllBadgesSeen()
    }

    private fun refreshBadgesScreen() {
        GamificationStore.refreshGoalPeriods()

        val expTitleText = findViewById<TextView>(R.id.expTitleText)
        val xpPercentText = findViewById<TextView>(R.id.xpPercentText)
        val xpFillBar = findViewById<View>(R.id.xpFillBar)
        val xpHandle = findViewById<View>(R.id.xpHandle)
        val dailyGoalText = findViewById<TextView>(R.id.dailyGoalText)
        val weeklyGoalText = findViewById<TextView>(R.id.weeklyGoalText)
        val badgeGrid = findViewById<GridLayout>(R.id.badgeGrid)

        val level = GamificationStore.level()
        val intoLevel = GamificationStore.xpIntoLevel()
        val needed = GamificationStore.xpForNextLevel()
        val percent = GamificationStore.progressPercent()

        expTitleText.text = "EXP · Level $level · $intoLevel / $needed"
        xpPercentText.text = "$percent%"

        // fill the XP bar after layout so we know the track width
        xpFillBar.post {
            val trackWidth = (xpFillBar.parent as View).width
            val fillWidth = (trackWidth * (percent / 100f)).toInt().coerceAtLeast(0)

            val fillParams = xpFillBar.layoutParams
            fillParams.width = fillWidth
            xpFillBar.layoutParams = fillParams

            val handleParams = xpHandle.layoutParams as FrameLayout.LayoutParams
            handleParams.marginStart = (fillWidth - 2).coerceAtLeast(0)
            xpHandle.layoutParams = handleParams
        }

        // completed goals get a gold gradient highlight
        if (GamificationStore.isDailyComplete()) {
            dailyGoalText.setBackgroundResource(R.drawable.goal_complete_bg)
            dailyGoalText.setTextColor(0xFF1E1E1E.toInt())
            dailyGoalText.text =
                "Daily goal complete: Add 1 expense (${GamificationStore.dailyProgress()}/${GamificationStore.dailyTarget()})"
        } else {
            dailyGoalText.background = null
            dailyGoalText.setTextColor(0xFF333333.toInt())
            dailyGoalText.text =
                "Daily goal: Add 1 expense (${GamificationStore.dailyProgress()}/${GamificationStore.dailyTarget()})"
        }

        if (GamificationStore.isWeeklyComplete()) {
            weeklyGoalText.setBackgroundResource(R.drawable.goal_complete_bg)
            weeklyGoalText.setTextColor(0xFF1E1E1E.toInt())
            weeklyGoalText.text =
                "Weekly goal complete: Add 5 expenses (${GamificationStore.weeklyProgress()}/${GamificationStore.weeklyTarget()})"
        } else {
            weeklyGoalText.background = null
            weeklyGoalText.setTextColor(0xFF333333.toInt())
            weeklyGoalText.text =
                "Weekly goal: Add 5 expenses (${GamificationStore.weeklyProgress()}/${GamificationStore.weeklyTarget()})"
        }

        badgeGrid.removeAllViews()

        val badges = GamificationStore.allBadges()
        val columnWidth = resources.displayMetrics.widthPixels -
            (48 * resources.displayMetrics.density).toInt()
        val tileWidth = columnWidth / 2

        badges.forEachIndexed { index, badge ->
            val tile = layoutInflater.inflate(R.layout.item_badge, badgeGrid, false)
            val background = tile.findViewById<View>(R.id.badgeTileBackground)
            val icon = tile.findViewById<ImageView>(R.id.badgeIcon)
            val notificationDot = tile.findViewById<View>(R.id.badgeNotificationDot)
            val unlocked = GamificationStore.isBadgeUnlocked(badge.id)
            val unseen = GamificationStore.isBadgeUnseen(badge.id)

            if (unlocked) {
                background.setBackgroundResource(R.drawable.badge_tile_unlocked)
                icon.setImageResource(badge.iconRes)
                icon.visibility = View.VISIBLE
            } else {
                background.setBackgroundResource(R.drawable.badge_tile_locked)
                icon.setImageResource(R.drawable.ic_badge_lock)
                icon.visibility = View.VISIBLE
            }

            // red corner dot only for newly unlocked badges
            notificationDot.visibility = if (unlocked && unseen) View.VISIBLE else View.GONE

            tile.setOnClickListener {
                if (unlocked) {
                    showBadgePopup(badge)
                } else {
                    Toast.makeText(
                        this,
                        "Locked: ${badge.title}. ${badge.description}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = tileWidth - (8 * resources.displayMetrics.density).toInt()
                height = (145 * resources.displayMetrics.density).toInt()
                columnSpec = GridLayout.spec(index % 2)
                rowSpec = GridLayout.spec(index / 2)
                setGravity(Gravity.CENTER)
                leftMargin = if (index % 2 == 0) 0 else (8 * resources.displayMetrics.density).toInt()
                rightMargin = if (index % 2 == 0) (8 * resources.displayMetrics.density).toInt() else 0
                bottomMargin = (16 * resources.displayMetrics.density).toInt()
            }

            badgeGrid.addView(tile, params)
        }

        NavHighlight.updateBadgesDot(this)
    }

    // centered popup showcasing an unlocked badge
    private fun showBadgePopup(badge: BadgeInfo) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_badge_detail, null)
        val icon = dialogView.findViewById<ImageView>(R.id.badgePopupIcon)
        val title = dialogView.findViewById<TextView>(R.id.badgePopupTitle)
        val requirement = dialogView.findViewById<TextView>(R.id.badgePopupRequirement)
        val closeButton = dialogView.findViewById<TextView>(R.id.badgePopupCloseButton)

        icon.setImageResource(badge.iconRes)
        title.text = badge.title
        requirement.text = "Unlocked by: ${badge.description}\nXP reward: +30"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
