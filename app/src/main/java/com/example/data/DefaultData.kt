package com.example.data

import com.example.model.MacroProfile
import com.example.model.TargetActionType
import com.example.model.TargetPoint

object DefaultData {
    val defaultMultiTargetPipeline = listOf(
        TargetPoint(
            id = 1,
            name = "Open Chest",
            type = TargetActionType.TAP,
            x = 180f,
            y = 420f,
            delayMs = 500L,
            holdDurationMs = 30L
        ),
        TargetPoint(
            id = 2,
            name = "Scroll Feed",
            type = TargetActionType.SWIPE,
            x = 500f,
            y = 800f,
            swipeEndX = 500f,
            swipeEndY = 300f,
            swipeDurationMs = 400L,
            delayMs = 400L
        ),
        TargetPoint(
            id = 3,
            name = "Confirm Claim",
            type = TargetActionType.TAP,
            x = 840f,
            y = 1600f,
            delayMs = 1200L,
            holdDurationMs = 50L
        ),
        TargetPoint(
            id = 4,
            name = "Standby Pause",
            type = TargetActionType.WAIT,
            x = 540f,
            y = 960f,
            delayMs = 2000L
        ),
        TargetPoint(
            id = 5,
            name = "Boss Attack Burst",
            type = TargetActionType.BURST,
            x = 540f,
            y = 960f,
            burstCount = 5,
            burstCadenceMs = 20L,
            delayMs = 200L
        )
    )

    val defaultProfiles = listOf(
        MacroProfile(
            id = "rpg_loot",
            title = "RPG Auto-Loot & Battle Macro",
            category = "Gaming",
            categoryTag = "AUTO-CYCLE",
            description = "Multi-stage raid cycle with auto-potion and inventory clearance.",
            targetsCount = 6,
            cycleDelayMs = 120L,
            lastRunText = "12 mins ago",
            isActive = true,
            points = defaultMultiTargetPipeline
        ),
        MacroProfile(
            id = "shorts_swiper",
            title = "Shorts / Reels Auto Swiper",
            category = "Social",
            categoryTag = "Jitter On",
            description = "Natural curved swipe vectors with 15% random auto-like cadence.",
            targetsCount = 2,
            cycleDelayMs = 8500L,
            lastRunText = "1 hour ago",
            isActive = true
        ),
        MacroProfile(
            id = "flash_sale",
            title = "Flash Sale Sniper (Sub-millisecond)",
            category = "Shopping",
            categoryTag = "CLOCK-SYNC",
            description = "Precise system epoch alignment for rapid checkout button triggering.",
            targetsCount = 3,
            cycleDelayMs = 5L,
            lastRunText = "Scheduled: In 4h 18m",
            isActive = false,
            isSniper = true,
            sniperTimeText = "Launch: 12:00:00.000 AM • 5 ms BURST"
        ),
        MacroProfile(
            id = "cookie_bot",
            title = "Cookie & Energy Clicker Bot",
            category = "Idle Game",
            categoryTag = "TURBO",
            description = "Dual coordinates alternation designed for maximum tap-reward speed.",
            targetsCount = 2,
            cycleDelayMs = 50L,
            lastRunText = "2 hours ago",
            isActive = true,
            totalLoops = 50000L
        )
    )
}
