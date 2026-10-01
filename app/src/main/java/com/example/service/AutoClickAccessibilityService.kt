package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class AutoClickAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceBound.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility event processing if needed
    }

    override fun onInterrupt() {
        instance = null
        _isServiceBound.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isServiceBound.value = false
        }
    }

    fun dispatchTap(x: Float, y: Float, holdDurationMs: Long, antiBanJitter: Boolean = false, callback: ((Boolean) -> Unit)? = null) {
        val jitterX = if (antiBanJitter) (Random.nextFloat() * 10f - 5f) else 0f
        val jitterY = if (antiBanJitter) (Random.nextFloat() * 10f - 5f) else 0f
        val finalX = (x + jitterX).coerceAtLeast(0f)
        val finalY = (y + jitterY).coerceAtLeast(0f)

        val path = Path().apply {
            moveTo(finalX, finalY)
        }

        val duration = holdDurationMs.coerceIn(10L, 1000L)
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                callback?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                callback?.invoke(false)
            }
        }, null)
    }

    fun dispatchSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long, callback: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val duration = durationMs.coerceIn(50L, 3000L)
        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                callback?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                callback?.invoke(false)
            }
        }, null)
    }

    companion object {
        var instance: AutoClickAccessibilityService? = null
            private set

        private val _isServiceBound = MutableStateFlow(false)
        val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${AutoClickAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true) ||
                    componentName.contains(AutoClickAccessibilityService::class.java.simpleName)) {
                    return true
                }
            }
            return instance != null
        }
    }
}
