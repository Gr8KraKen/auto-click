package com.example.model

enum class TargetActionType {
    TAP,
    SWIPE,
    WAIT,
    BURST
}

data class TargetPoint(
    val id: Int,
    val name: String,
    val type: TargetActionType = TargetActionType.TAP,
    val x: Float = 320f,
    val y: Float = 480f,
    val delayMs: Long = 100L,
    val holdDurationMs: Long = 30L,
    val swipeEndX: Float? = null,
    val swipeEndY: Float? = null,
    val swipeDurationMs: Long = 400L,
    val burstCount: Int = 1,
    val burstCadenceMs: Long = 20L
)

data class MacroProfile(
    val id: String,
    val title: String,
    val category: String,
    val categoryTag: String = "AUTO-CYCLE",
    val description: String,
    val targetsCount: Int,
    val cycleDelayMs: Long = 100L,
    val lastRunText: String = "Never",
    val isActive: Boolean = true,
    val isSniper: Boolean = false,
    val sniperTimeText: String? = null,
    val totalLoops: Long = 50000L,
    val points: List<TargetPoint> = emptyList(),
    val infiniteLoop: Boolean = true,
    val antiBanShift: Boolean = true,
    val jitterPx: Int = 5
)

enum class ClickUnit {
    MS,
    SEC,
    MIN
}

enum class ExecutionLimitType {
    INDEFINITE,
    CYCLE_COUNT,
    DURATION_TIMER
}

data class SingleTargetConfig(
    val intervalMs: Long = 100L,
    val unit: ClickUnit = ClickUnit.MS,
    val holdLatencyMs: Long = 25L,
    val antiDetectionJitter: Boolean = true,
    val jitterMs: Int = 15,
    val executionLimit: ExecutionLimitType = ExecutionLimitType.INDEFINITE,
    val cycleCountLimit: Int = 1000,
    val durationTimerSeconds: Int = 900, // 15 mins
    val isFixedAnchor: Boolean = true,
    val anchorX: Int = 540,
    val anchorY: Int = 1120
)

data class OverlayConfig(
    val transparencyPercent: Int = 95,
    val showReticleCoordinates: Boolean = true,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val antiBanGaussian: Boolean = true,
    val dockSizeScale: Float = 1.0f
)
