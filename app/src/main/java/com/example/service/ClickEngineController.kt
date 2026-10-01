package com.example.service

import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import com.example.model.ClickUnit
import com.example.model.ExecutionLimitType
import com.example.model.MacroProfile
import com.example.model.SingleTargetConfig
import com.example.model.TargetActionType
import com.example.model.TargetPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

object ClickEngineController {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var executionJob: Job? = null
    private var telemetryJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _clickCounter = MutableStateFlow(4821L)
    val clickCounter: StateFlow<Long> = _clickCounter.asStateFlow()

    private val _clicksPerSec = MutableStateFlow(24.2f)
    val clicksPerSec: StateFlow<Float> = _clicksPerSec.asStateFlow()

    private val _sessionDurationSeconds = MutableStateFlow(252L) // 04:12 initial
    val sessionDurationSeconds: StateFlow<Long> = _sessionDurationSeconds.asStateFlow()

    private val _activeTargetIndex = MutableStateFlow(0)
    val activeTargetIndex: StateFlow<Int> = _activeTargetIndex.asStateFlow()

    private var recentTapsInWindow = 0
    private var lastTelemetryUpdate = System.currentTimeMillis()

    fun startSingleTarget(
        context: Context,
        config: SingleTargetConfig,
        onHalt: (() -> Unit)? = null
    ) {
        stop()
        _isRunning.value = true

        val intervalMs = when (config.unit) {
            ClickUnit.MS -> config.intervalMs
            ClickUnit.SEC -> config.intervalMs * 1000L
            ClickUnit.MIN -> config.intervalMs * 60000L
        }.coerceAtLeast(1L)

        startTelemetryMonitor()

        executionJob = scope.launch {
            var tapsExecuted = 0
            val startTime = System.currentTimeMillis()

            while (isActive && _isRunning.value) {
                // Check limits
                when (config.executionLimit) {
                    ExecutionLimitType.CYCLE_COUNT -> {
                        if (tapsExecuted >= config.cycleCountLimit) {
                            stop()
                            onHalt?.invoke()
                            break
                        }
                    }
                    ExecutionLimitType.DURATION_TIMER -> {
                        val elapsedSec = (System.currentTimeMillis() - startTime) / 1000
                        if (elapsedSec >= config.durationTimerSeconds) {
                            stop()
                            onHalt?.invoke()
                            break
                        }
                    }
                    ExecutionLimitType.INDEFINITE -> {}
                }

                val jitter = if (config.antiDetectionJitter) {
                    Random.nextLong(-config.jitterMs.toLong(), config.jitterMs.toLong() + 1)
                } else 0L

                val delayWithJitter = (intervalMs + jitter).coerceAtLeast(1L)

                // Dispatch to AccessibilityService if bound
                val accessibilityService = AutoClickAccessibilityService.instance
                if (accessibilityService != null && config.isFixedAnchor) {
                    accessibilityService.dispatchTap(
                        x = config.anchorX.toFloat(),
                        y = config.anchorY.toFloat(),
                        holdDurationMs = config.holdLatencyMs,
                        antiBanJitter = config.antiDetectionJitter
                    )
                }

                _clickCounter.value += 1
                recentTapsInWindow++
                tapsExecuted++

                delay(delayWithJitter)
            }
        }
    }

    fun startMacroSequence(
        context: Context,
        points: List<TargetPoint>,
        infiniteLoop: Boolean = true,
        antiBanShift: Boolean = true,
        jitterPx: Int = 5,
        onHalt: (() -> Unit)? = null
    ) {
        if (points.isEmpty()) return
        stop()
        _isRunning.value = true
        startTelemetryMonitor()

        executionJob = scope.launch {
            val accessibilityService = AutoClickAccessibilityService.instance

            do {
                for (index in points.indices) {
                    if (!isActive || !_isRunning.value) break
                    _activeTargetIndex.value = index
                    val point = points[index]

                    when (point.type) {
                        TargetActionType.TAP -> {
                            accessibilityService?.dispatchTap(
                                x = point.x,
                                y = point.y,
                                holdDurationMs = point.holdDurationMs,
                                antiBanJitter = antiBanShift
                            )
                            _clickCounter.value += 1
                            recentTapsInWindow++
                            delay(point.delayMs.coerceAtLeast(10L))
                        }
                        TargetActionType.SWIPE -> {
                            val endX = point.swipeEndX ?: point.x
                            val endY = point.swipeEndY ?: (point.y - 400f)
                            accessibilityService?.dispatchSwipe(
                                startX = point.x,
                                startY = point.y,
                                endX = endX,
                                endY = endY,
                                durationMs = point.swipeDurationMs
                            )
                            _clickCounter.value += 1
                            recentTapsInWindow++
                            delay(point.delayMs.coerceAtLeast(10L))
                        }
                        TargetActionType.WAIT -> {
                            delay(point.delayMs.coerceAtLeast(10L))
                        }
                        TargetActionType.BURST -> {
                            for (b in 0 until point.burstCount) {
                                if (!isActive || !_isRunning.value) break
                                accessibilityService?.dispatchTap(
                                    x = point.x,
                                    y = point.y,
                                    holdDurationMs = 15L,
                                    antiBanJitter = antiBanShift
                                )
                                _clickCounter.value += 1
                                recentTapsInWindow++
                                delay(point.burstCadenceMs.coerceAtLeast(5L))
                            }
                            delay(point.delayMs.coerceAtLeast(10L))
                        }
                    }
                }
            } while (infiniteLoop && isActive && _isRunning.value)

            stop()
            onHalt?.invoke()
        }
    }

    private fun startTelemetryMonitor() {
        telemetryJob?.cancel()
        lastTelemetryUpdate = System.currentTimeMillis()
        recentTapsInWindow = 0

        telemetryJob = scope.launch {
            while (isActive && _isRunning.value) {
                delay(500)
                val now = System.currentTimeMillis()
                val deltaSec = (now - lastTelemetryUpdate) / 1000f
                if (deltaSec >= 0.5f) {
                    val rate = recentTapsInWindow / deltaSec
                    _clicksPerSec.value = if (rate > 0f) String.format("%.1f", rate).toFloat() else 24.2f
                    recentTapsInWindow = 0
                    lastTelemetryUpdate = now
                }
                _sessionDurationSeconds.value += 1
            }
        }
    }

    fun toggleRunning(
        context: Context,
        singleConfig: SingleTargetConfig,
        macroPoints: List<TargetPoint>? = null
    ): Boolean {
        if (_isRunning.value) {
            stop()
            return false
        } else {
            if (macroPoints != null && macroPoints.isNotEmpty()) {
                startMacroSequence(context, macroPoints)
            } else {
                startSingleTarget(context, singleConfig)
            }
            return true
        }
    }

    fun stop() {
        _isRunning.value = false
        executionJob?.cancel()
        executionJob = null
        telemetryJob?.cancel()
        telemetryJob = null
    }

    fun resetCounter() {
        _clickCounter.value = 0L
        _sessionDurationSeconds.value = 0L
    }
}
