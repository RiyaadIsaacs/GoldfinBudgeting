package com.example.goldfinbudgeting

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import java.util.Calendar

// Badge definition shown on the Badges screen
data class BadgeInfo(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int
)

// Result after awarding XP so the UI can show a short toast if needed
data class XpAwardResult(
    val xpGained: Int,
    val leveledUp: Boolean,
    val newLevel: Int,
    val newBadges: List<BadgeInfo>
)

// Simple per user XP goals and badges
// Kept quiet: XP fills the bar silently. Only level ups and new badges toast
object GamificationStore {
    private const val PREFS = "goldfin_gamification"
    private const val XP_PER_LEVEL = 100

    private const val XP_ADD_EXPENSE = 10
    private const val XP_ADD_ENVELOPE = 15
    private const val XP_DAILY_BONUS = 25
    private const val XP_WEEKLY_BONUS = 75
    private const val XP_FIRST_GOALS = 20
    private const val XP_PER_BADGE = 30

    private const val DAILY_TARGET = 1
    private const val WEEKLY_TARGET = 5

    @Volatile
    private var appContext: Context? = null

    fun bind(context: Context) {
        appContext = context.applicationContext
    }

    private fun ctx(): Context {
        return appContext
            ?: throw IllegalStateException("GamificationStore used before GoldfinApp bind")
    }

    private fun prefs(): SharedPreferences {
        return ctx().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    private fun userKey(suffix: String): String {
        return "${AccountStore.currentUserId(ctx())}_$suffix"
    }

    fun totalXp(): Int = prefs().getInt(userKey("total_xp"), 0)

    fun level(): Int = 1 + totalXp() / XP_PER_LEVEL

    fun xpIntoLevel(): Int = totalXp() % XP_PER_LEVEL

    fun xpForNextLevel(): Int = XP_PER_LEVEL

    fun progressPercent(): Int {
        return ((xpIntoLevel().toDouble() / XP_PER_LEVEL) * 100).toInt().coerceIn(0, 100)
    }

    fun expenseCount(): Int = prefs().getInt(userKey("expense_count"), 0)

    fun envelopeCount(): Int = prefs().getInt(userKey("envelope_count"), 0)

    fun dailyProgress(): Int = prefs().getInt(userKey("daily_progress"), 0).coerceAtMost(DAILY_TARGET)

    fun dailyTarget(): Int = DAILY_TARGET

    fun weeklyProgress(): Int = prefs().getInt(userKey("weekly_progress"), 0).coerceAtMost(WEEKLY_TARGET)

    fun weeklyTarget(): Int = WEEKLY_TARGET

    fun isDailyComplete(): Boolean = dailyProgress() >= DAILY_TARGET

    fun isWeeklyComplete(): Boolean = weeklyProgress() >= WEEKLY_TARGET

    fun unlockedBadgeIds(): Set<String> {
        return prefs().getStringSet(userKey("badges"), emptySet()) ?: emptySet()
    }

    fun isBadgeUnlocked(badgeId: String): Boolean {
        return unlockedBadgeIds().contains(badgeId)
    }

    fun unseenBadgeIds(): Set<String> {
        return prefs().getStringSet(userKey("unseen_badges"), emptySet()) ?: emptySet()
    }

    fun hasUnseenBadges(): Boolean = unseenBadgeIds().isNotEmpty()

    fun isBadgeUnseen(badgeId: String): Boolean = unseenBadgeIds().contains(badgeId)

    // Clear the drawer red dot and badge corner dots after visiting Badges
    fun markAllBadgesSeen() {
        prefs().edit().putStringSet(userKey("unseen_badges"), emptySet()).commit()
    }

    fun dailyGoalLabel(): String {
        return "Add 1 expense (${dailyProgress()}/${dailyTarget()})"
    }

    // Show the daily task message once per day if it is not done yet
    fun shouldShowDailyPrompt(): Boolean {
        refreshGoalPeriods()
        if (isDailyComplete()) {
            return false
        }

        val calendar = Calendar.getInstance()
        val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        return prefs().getString(userKey("daily_prompt_key"), null) != dayKey
    }

    fun markDailyPromptShown() {
        val calendar = Calendar.getInstance()
        val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        prefs().edit().putString(userKey("daily_prompt_key"), dayKey).commit()
    }

    // All badges in display order for the grid
    fun allBadges(): List<BadgeInfo> {
        return listOf(
            BadgeInfo("first_expense", "First Expense", "Add your first expense", R.drawable.ic_badge_stop),
            BadgeInfo("first_envelope", "Envelope Maker", "Add your first envelope", R.drawable.ic_badge_clock),
            BadgeInfo("expenses_5", "Getting Going", "Add 5 expenses", R.drawable.ic_badge_star),
            BadgeInfo("expenses_20", "Habit Builder", "Add 20 expenses", R.drawable.ic_badge_star),
            BadgeInfo("expenses_50", "Catalog Pro", "Add 50 expenses", R.drawable.ic_badge_star),
            BadgeInfo("level_5", "Rising Star", "Reach level 5", R.drawable.ic_badge_star),
            BadgeInfo("level_10", "Budget Boss", "Reach level 10", R.drawable.ic_badge_star),
            BadgeInfo("weekly_warrior", "Weekly Warrior", "Finish a weekly goal", R.drawable.ic_badge_star)
        )
    }

    // Called when the user adds a new expense
    fun onExpenseAdded(): XpAwardResult {
        refreshGoalPeriods()

        val editor = prefs().edit()
        val newCount = expenseCount() + 1
        editor.putInt(userKey("expense_count"), newCount)

        var xpGained = XP_ADD_EXPENSE
        val levelBefore = level()
        var weeklyJustDone = false

        // daily goal: add 1 expense today
        val dailyBefore = dailyProgress()
        if (dailyBefore < DAILY_TARGET) {
            val dailyAfter = dailyBefore + 1
            editor.putInt(userKey("daily_progress"), dailyAfter)
            if (dailyAfter >= DAILY_TARGET) {
                xpGained += XP_DAILY_BONUS
            }
        }

        // weekly goal: add 5 expenses this week
        val weeklyBefore = weeklyProgress()
        if (weeklyBefore < WEEKLY_TARGET) {
            val weeklyAfter = weeklyBefore + 1
            editor.putInt(userKey("weekly_progress"), weeklyAfter)
            if (weeklyAfter >= WEEKLY_TARGET) {
                xpGained += XP_WEEKLY_BONUS
                editor.putBoolean(userKey("weekly_ever_done"), true)
                weeklyJustDone = true
            }
        }

        val xpBefore = totalXp()
        editor.putInt(userKey("total_xp"), xpBefore + xpGained)
        editor.commit()

        val newBadges = unlockEligibleBadges(
            expenseCount = newCount,
            envelopeCount = envelopeCount(),
            level = level(),
            weeklyEverDone = weeklyJustDone || prefs().getBoolean(userKey("weekly_ever_done"), false)
        )

        // badge unlocks may have added more XP on top of the base award
        val xpAfter = totalXp()
        val levelAfter = level()

        return XpAwardResult(
            xpGained = xpAfter - xpBefore,
            leveledUp = levelAfter > levelBefore,
            newLevel = levelAfter,
            newBadges = newBadges
        )
    }

    // Called when the user adds a new envelope
    fun onEnvelopeAdded(): XpAwardResult {
        refreshGoalPeriods()

        val editor = prefs().edit()
        val newEnvelopeCount = envelopeCount() + 1
        editor.putInt(userKey("envelope_count"), newEnvelopeCount)

        val levelBefore = level()
        val xpBefore = totalXp()
        editor.putInt(userKey("total_xp"), xpBefore + XP_ADD_ENVELOPE)
        editor.commit()

        val newBadges = unlockEligibleBadges(
            expenseCount = expenseCount(),
            envelopeCount = newEnvelopeCount,
            level = level(),
            weeklyEverDone = prefs().getBoolean(userKey("weekly_ever_done"), false)
        )

        val xpAfter = totalXp()
        val levelAfter = level()

        return XpAwardResult(
            xpGained = xpAfter - xpBefore,
            leveledUp = levelAfter > levelBefore,
            newLevel = levelAfter,
            newBadges = newBadges
        )
    }

    // Small reward the first time they save monthly goals
    fun onMonthlyGoalsSaved(): XpAwardResult {
        if (prefs().getBoolean(userKey("goals_done"), false)) {
            return XpAwardResult(0, false, level(), emptyList())
        }

        val levelBefore = level()
        val xpBefore = totalXp()
        prefs().edit()
            .putBoolean(userKey("goals_done"), true)
            .putInt(userKey("total_xp"), xpBefore + XP_FIRST_GOALS)
            .commit()

        val newBadges = unlockEligibleBadges(
            expenseCount = expenseCount(),
            envelopeCount = envelopeCount(),
            level = level(),
            weeklyEverDone = prefs().getBoolean(userKey("weekly_ever_done"), false)
        )

        val xpAfter = totalXp()
        val levelAfter = level()

        return XpAwardResult(
            xpGained = xpAfter - xpBefore,
            leveledUp = levelAfter > levelBefore,
            newLevel = levelAfter,
            newBadges = newBadges
        )
    }

    // Short toast only for level ups or brand new badges
    fun showFeedback(context: Context, result: XpAwardResult) {
        val message = when {
            result.leveledUp && result.newBadges.isNotEmpty() -> {
                val bonus = result.newBadges.size * XP_PER_BADGE
                "Level ${result.newLevel}! Badge: ${result.newBadges.first().title} (+$bonus XP)"
            }
            result.leveledUp ->
                "Level up! You reached level ${result.newLevel}"
            result.newBadges.isNotEmpty() -> {
                val bonus = result.newBadges.size * XP_PER_BADGE
                "Badge unlocked: ${result.newBadges.first().title} (+$bonus XP)"
            }
            else -> return
        }

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Reset daily or weekly counters when the calendar day or week changes
    fun refreshGoalPeriods() {
        val calendar = Calendar.getInstance()
        val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        val weekKey = "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"

        val editor = prefs().edit()

        if (prefs().getString(userKey("daily_key"), null) != dayKey) {
            editor.putString(userKey("daily_key"), dayKey)
            editor.putInt(userKey("daily_progress"), 0)
        }

        if (prefs().getString(userKey("weekly_key"), null) != weekKey) {
            editor.putString(userKey("weekly_key"), weekKey)
            editor.putInt(userKey("weekly_progress"), 0)
        }

        editor.commit()
    }

    private fun unlockEligibleBadges(
        expenseCount: Int,
        envelopeCount: Int,
        level: Int,
        weeklyEverDone: Boolean
    ): List<BadgeInfo> {
        val unlocked = unlockedBadgeIds().toMutableSet()
        val newlyUnlocked = mutableListOf<BadgeInfo>()

        fun tryUnlock(badge: BadgeInfo, condition: Boolean) {
            if (condition && unlocked.add(badge.id)) {
                newlyUnlocked.add(badge)
            }
        }

        val badges = allBadges().associateBy { it.id }

        tryUnlock(badges.getValue("first_expense"), expenseCount >= 1)
        tryUnlock(badges.getValue("first_envelope"), envelopeCount >= 1)
        tryUnlock(badges.getValue("expenses_5"), expenseCount >= 5)
        tryUnlock(badges.getValue("expenses_20"), expenseCount >= 20)
        tryUnlock(badges.getValue("expenses_50"), expenseCount >= 50)
        tryUnlock(badges.getValue("level_5"), level >= 5)
        tryUnlock(badges.getValue("level_10"), level >= 10)
        tryUnlock(badges.getValue("weekly_warrior"), weeklyEverDone)

        if (newlyUnlocked.isNotEmpty()) {
            val unseen = unseenBadgeIds().toMutableSet()
            newlyUnlocked.forEach { unseen.add(it.id) }

            // each newly unlocked badge also grants XP
            val bonusXp = newlyUnlocked.size * XP_PER_BADGE
            val xpAfterBadges = totalXp() + bonusXp

            prefs().edit()
                .putStringSet(userKey("badges"), unlocked)
                .putStringSet(userKey("unseen_badges"), unseen)
                .putInt(userKey("total_xp"), xpAfterBadges)
                .commit()

            // badge XP may push them into a new level badge
            val more = unlockEligibleBadges(
                expenseCount = expenseCount,
                envelopeCount = envelopeCount,
                level = 1 + xpAfterBadges / XP_PER_LEVEL,
                weeklyEverDone = weeklyEverDone
            )

            return newlyUnlocked + more
        }

        return newlyUnlocked
    }
}
