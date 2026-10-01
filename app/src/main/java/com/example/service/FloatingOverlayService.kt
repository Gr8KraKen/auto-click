package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.TargetActionType
import com.example.model.TargetPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var dockView: View? = null
    private val targetViews = mutableListOf<View>()
    private val targetsList = mutableListOf<TargetPoint>()

    private var areTargetsVisible = true
    private var isPlaying = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        _isOverlayRunning.value = true

        initDefaultTargets()
        showDockView()
        showTargetViews()
    }

    private fun initDefaultTargets() {
        targetsList.clear()
        targetsList.add(TargetPoint(id = 1, name = "Target 1", type = TargetActionType.TAP, x = 300f, y = 600f))
        targetsList.add(TargetPoint(id = 2, name = "Target 2", type = TargetActionType.TAP, x = 400f, y = 900f))
    }

    private fun showDockView() {
        if (dockView != null) return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 30
            y = 250
        }

        // Programmatically build vertical dock matching obsidian precision theme
        val dockLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(16, 16, 16, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6161B22"))
                cornerRadius = 60f
                setStroke(3, Color.parseColor("#4000F2FE"))
            }
        }

        // Drag handle
        val handle = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(40, 10).apply {
                bottomMargin = 14
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#849495"))
                cornerRadius = 6f
            }
        }
        dockLayout.addView(handle)

        // Play/Pause button
        val playButton = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(110, 110).apply {
                bottomMargin = 16
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#00F2FE"))
            }
            val icon = ImageView(context).apply {
                setImageResource(android.R.drawable.ic_media_play)
                setColorFilter(Color.parseColor("#00373A"))
                layoutParams = FrameLayout.LayoutParams(60, 60, Gravity.CENTER)
            }
            addView(icon)

            setOnClickListener {
                isPlaying = !isPlaying
                if (isPlaying) {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor("#70FFBA"))
                    }
                    icon.setImageResource(android.R.drawable.ic_media_pause)
                    ClickEngineController.startMacroSequence(context, targetsList)
                } else {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor("#00F2FE"))
                    }
                    icon.setImageResource(android.R.drawable.ic_media_play)
                    ClickEngineController.stop()
                }
            }
        }
        dockLayout.addView(playButton)

        // Add Target button (+)
        val addButton = createDockIconButton(android.R.drawable.ic_input_add, "#DFE2EB") {
            val newId = targetsList.size + 1
            val newTarget = TargetPoint(id = newId, name = "Target $newId", type = TargetActionType.TAP, x = 350f, y = 700f)
            targetsList.add(newTarget)
            addTargetView(newTarget)
        }
        dockLayout.addView(addButton)

        // Remove Target button (-)
        val removeButton = createDockIconButton(android.R.drawable.ic_delete, "#DFE2EB") {
            if (targetsList.isNotEmpty()) {
                targetsList.removeAt(targetsList.size - 1)
                if (targetViews.isNotEmpty()) {
                    val lastView = targetViews.removeAt(targetViews.size - 1)
                    try { windowManager.removeView(lastView) } catch (_: Exception) {}
                }
            }
        }
        dockLayout.addView(removeButton)

        // Eye visibility toggle
        val eyeButton = createDockIconButton(android.R.drawable.ic_menu_view, "#DFE2EB") {
            areTargetsVisible = !areTargetsVisible
            for (tv in targetViews) {
                tv.visibility = if (areTargetsVisible) View.VISIBLE else View.GONE
            }
        }
        dockLayout.addView(eyeButton)

        // Settings / Open App button
        val appButton = createDockIconButton(android.R.drawable.ic_menu_preferences, "#DFE2EB") {
            val launchIntent = Intent(this@FloatingOverlayService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(launchIntent)
        }
        dockLayout.addView(appButton)

        // Close / Stop overlay button
        val closeButton = createDockIconButton(android.R.drawable.ic_menu_close_clear_cancel, "#FF5C77") {
            stopSelf()
        }
        dockLayout.addView(closeButton)

        // Touch listener for dragging dock
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        dockLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX - (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    try { windowManager.updateViewLayout(dockLayout, params) } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }

        dockView = dockLayout
        try {
            windowManager.addView(dockLayout, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDockIconButton(resId: Int, colorHex: String, onClick: () -> Unit): View {
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(95, 95).apply {
                bottomMargin = 10
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#262A31"))
            }
            val icon = ImageView(context).apply {
                setImageResource(resId)
                setColorFilter(Color.parseColor(colorHex))
                layoutParams = FrameLayout.LayoutParams(50, 50, Gravity.CENTER)
            }
            addView(icon)
            setOnClickListener { onClick() }
        }
    }

    private fun showTargetViews() {
        for (target in targetsList) {
            addTargetView(target)
        }
    }

    private fun addTargetView(target: TargetPoint) {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val targetParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = target.x.toInt()
            y = target.y.toInt()
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Circular reticle
        val reticle = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(110, 110)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#4000F2FE"))
                setStroke(4, Color.parseColor("#00F2FE"))
            }
            val innerDot = TextView(context).apply {
                text = target.id.toString()
                setTextColor(Color.parseColor("#00373A"))
                textSize = 14f
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#00F2FE"))
                }
                layoutParams = FrameLayout.LayoutParams(60, 60, Gravity.CENTER)
            }
            addView(innerDot)
        }
        container.addView(reticle)

        // Latency tag
        val tag = TextView(this).apply {
            text = "${target.delayMs}ms"
            setTextColor(Color.parseColor("#DFE2EB"))
            textSize = 10f
            setPadding(12, 4, 12, 4)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#CC1C2026"))
                cornerRadius = 10f
            }
        }
        container.addView(tag)

        var initX = 0
        var initY = 0
        var touchX = 0f
        var touchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = targetParams.x
                    initY = targetParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    targetParams.x = initX + (event.rawX - touchX).toInt()
                    targetParams.y = initY + (event.rawY - touchY).toInt()
                    try { windowManager.updateViewLayout(container, targetParams) } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val idx = targetsList.indexOfFirst { it.id == target.id }
                    if (idx != -1) {
                        targetsList[idx] = targetsList[idx].copy(x = targetParams.x.toFloat(), y = targetParams.y.toFloat())
                    }
                    true
                }
                else -> false
            }
        }

        targetViews.add(container)
        try {
            windowManager.addView(container, targetParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ClickEngineController.stop()
        dockView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        dockView = null
        for (tv in targetViews) {
            try { windowManager.removeView(tv) } catch (_: Exception) {}
        }
        targetViews.clear()
        _isOverlayRunning.value = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ClickForge Floating Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "ClickForge Floating HUD Overlay Active"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ClickForge Overlay Active")
            .setContentText("Auto Clicker floating HUD is running.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 2026
        private const val CHANNEL_ID = "clickforge_overlay_channel"

        private val _isOverlayRunning = MutableStateFlow(false)
        val isOverlayRunning = _isOverlayRunning.asStateFlow()

        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, FloatingOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
