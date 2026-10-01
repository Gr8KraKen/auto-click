package com.example.ui.screens

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.service.ClickEngineController
import com.example.ui.theme.CriticalCoral
import com.example.ui.theme.CriticalCoralContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonMintDark
import com.example.ui.theme.NeonMintFixed
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceBright
import com.example.ui.theme.ObsidianSurfaceCard
import com.example.ui.theme.ObsidianSurfaceDim
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianSurfaceHighest
import com.example.ui.theme.ObsidianSurfaceLow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TriggerAmber
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class DraggableReticle(
    val id: Int,
    var x: Float,
    var y: Float,
    val label: String,
    val delayText: String,
    val isSwipe: Boolean = false,
    val color: Color = ElectricCyan
)

@Composable
fun FloatingHudScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(true) }
    var tapCounter by remember { mutableLongStateOf(4868L) }
    var sessionSeconds by remember { mutableLongStateOf(254L) }
    var telemetryExpanded by remember { mutableStateOf(true) }

    // Dock parameters
    var intervalDelay by remember { mutableIntStateOf(40) }
    var dockOpacity by remember { mutableFloatStateOf(0.95f) }
    var showCoordinates by remember { mutableStateOf(true) }
    var reticlesVisible by remember { mutableStateOf(true) }

    // Draggable targets in game viewport
    val reticles = remember {
        mutableStateListOf(
            DraggableReticle(1, 80f, 120f, "Tap Point A", "100ms", false, ElectricCyan),
            DraggableReticle(2, 220f, 240f, "Swipe Point B", "250ms", true, NeonMint)
        )
    }

    // Live click simulation loop
    LaunchedEffect(isRunning, intervalDelay) {
        while (isRunning) {
            delay((intervalDelay * 2L).coerceAtLeast(40L))
            tapCounter += (1..3).random()
        }
    }

    // Live session clock
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            sessionSeconds++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hudPings")
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pingScale"
    )

    val lineDashOffset by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lineOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianSurface)
    ) {
        // Floating HUD Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(ObsidianSurface.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianSurfaceCard)
                        .testTag("hud_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.2f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Floating Hud Overlay",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = ElectricCyanDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Real-Time Execution Telemetry Pill
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceHigh.copy(alpha = 0.95f))
            ) {
                AnimatedVisibility(visible = telemetryExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active Engine
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(pingScale)
                                    .clip(CircleShape)
                                    .background(NeonMintFixed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "ACTIVE ENGINE",
                                    color = NeonMintFixed,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "24.2 clicks/sec",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(ObsidianSurfaceHighest))

                        // Counter
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "COUNTER",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%06d", tapCounter),
                                color = ElectricCyan,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(ObsidianSurfaceHighest))

                        // Session
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "SESSION",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            val m = (sessionSeconds / 60)
                            val s = (sessionSeconds % 60)
                            Text(
                                text = String.format("%02d:%02d", m, s),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = { telemetryExpanded = !telemetryExpanded },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianSurfaceHighest)
                        ) {
                            Icon(
                                imageVector = if (telemetryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Simulated Third-Party Game Viewport Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianSurfaceDim)
                    .border(1.dp, ObsidianSurfaceHighest, RoundedCornerShape(14.dp))
            ) {
                // Dimmed Underlay Game Scene (Hotlinked cyberpunk mobile RPG background)
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDC0DDJfsdTaPSvTuLepc1PSWZM3HbyPlFTybrV5wI6mBcH2V7OpNSUYHFVREonF59uw2h01SqT7MlUbm3EVx_xffcBIwZz-TkzycSkImC3mvCPWkELEzCs0CYtR5mFO9fEPxT07gTypABA7mTRpF2Nr3DR1x3dhqrFj_DBkfmuQlO5XDG4cAsepwmokEhF0_t4eClgz1k63vq1Rp42mF-PiNjrebd8lMr4IGklH2HVD9rUwr_gLe8_",
                    contentDescription = "Game Viewport Scene",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.45f)
                )

                // Top Vignette gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianSurface.copy(alpha = 0.7f),
                                    Color.Transparent,
                                    ObsidianSurface.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // In-World Game Decals
                // Gold coin tag
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianSurfaceDim.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Gold",
                        tint = TriggerAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "92,450 G",
                        color = TriggerAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Raid Level tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianSurfaceDim.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Raid",
                        tint = NeonMint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LVL 84 RAID",
                        color = NeonMint,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Boss Target Banner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurfaceCard.copy(alpha = 0.85f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Adjust,
                            contentDescription = "Target",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nexus Core [HP: 12%]",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Animated Vector Line between Target 1 and Target 2
                if (reticlesVisible && reticles.size >= 2) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val p1 = Offset(reticles[0].x + 30f, reticles[0].y + 30f)
                        val p2 = Offset(reticles[1].x + 30f, reticles[1].y + 30f)
                        drawLine(
                            brush = Brush.linearGradient(listOf(ElectricCyan, NeonMint)),
                            start = p1,
                            end = p2,
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), lineDashOffset)
                        )
                    }
                }

                // Draggable Reticles
                if (reticlesVisible) {
                    reticles.forEachIndexed { index, reticle ->
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(reticle.x.roundToInt(), reticle.y.roundToInt()) }
                                .pointerInput(reticle.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        reticle.x = (reticle.x + dragAmount.x).coerceIn(0f, 280f)
                                        reticle.y = (reticle.y + dragAmount.y).coerceIn(0f, 320f)
                                    }
                                }
                                .testTag("draggable_reticle_${reticle.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Halo Ring
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(reticle.color.copy(alpha = 0.2f))
                                        .border(2.dp, reticle.color, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Reticle Center Dot with Number
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(reticle.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = reticle.id.toString(),
                                            color = ObsidianSurfaceDim,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                if (showCoordinates) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ObsidianSurfaceHigh.copy(alpha = 0.9f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (reticle.isSwipe) Icons.Default.Swipe else Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = reticle.color,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = reticle.delayText,
                                                color = reticle.color,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Vertical Floating HUD Dock (Floating Dock on Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .alpha(dockOpacity)
                        .clip(RoundedCornerShape(32.dp))
                        .background(ObsidianSurfaceHigh.copy(alpha = 0.95f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                        .testTag("floating_hud_dock")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Drag Grip Handle
                        Column(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable {
                                    Toast.makeText(context, "Dock can be dragged over screen", Toast.LENGTH_SHORT).show()
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.size(width = 16.dp, height = 2.dp).clip(CircleShape).background(TextMuted))
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.size(width = 16.dp, height = 2.dp).clip(CircleShape).background(TextMuted))
                        }

                        // Play/Pause Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) ElectricCyan else ObsidianSurfaceHighest)
                                .clickable {
                                    isRunning = !isRunning
                                    Toast.makeText(context, if (isRunning) "Execution Resumed" else "Execution Paused", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("dock_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = if (isRunning) ElectricCyanDark else TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Add Target (+)
                        IconButton(
                            onClick = {
                                val nextId = reticles.size + 1
                                reticles.add(
                                    DraggableReticle(
                                        id = nextId,
                                        x = 100f + (nextId * 25f),
                                        y = 150f + (nextId * 25f),
                                        label = "Tap Point $nextId",
                                        delayText = "150ms",
                                        color = if (nextId % 2 == 0) NeonMint else ElectricCyan
                                    )
                                )
                                Toast.makeText(context, "Added Reticle $nextId", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceCard)
                                .testTag("dock_add_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = "Add Target",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Remove Target (-)
                        IconButton(
                            onClick = {
                                if (reticles.isNotEmpty()) {
                                    reticles.removeAt(reticles.size - 1)
                                    Toast.makeText(context, "Removed Target", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WrongLocation,
                                contentDescription = "Remove Target",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Eye Visibility Toggle
                        IconButton(
                            onClick = { reticlesVisible = !reticlesVisible },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceCard)
                        ) {
                            Icon(
                                imageVector = if (reticlesVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Reticles",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Tune / Settings Trigger
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Macro cadence fine-tuned", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Power / Kill Button
                        IconButton(
                            onClick = {
                                isRunning = false
                                Toast.makeText(context, "Overlay engine stopped", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CriticalCoralContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Power Off",
                                tint = CriticalCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Macro Profile & Speed Control Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianSurfaceHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Boss Dungeon Cycle",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Profile: Active Multi-Tap + Swipe",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonMint.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isRunning) "RUNNING" else "PAUSED",
                                color = if (isRunning) NeonMintFixed else TriggerAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Speed Control Stepper
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianSurfaceLow)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Click Interval Delay",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Per Target Cycle",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { intervalDelay = (intervalDelay - 5).coerceAtLeast(5) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ObsidianSurfaceHigh)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.width(70.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "$intervalDelay",
                                    color = ElectricCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "ms",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }

                            IconButton(
                                onClick = { intervalDelay = (intervalDelay + 5).coerceAtMost(500) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ObsidianSurfaceHigh)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // HUD Ergonomics & Opacity Adjuster Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Opacity,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Overlay Transparency",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${(dockOpacity * 100).toInt()}%",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BlurOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Slider(
                            value = dockOpacity,
                            onValueChange = { dockOpacity = it },
                            valueRange = 0.3f..1.0f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricCyan,
                                inactiveTrackColor = ObsidianSurfaceHighest
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show Reticle Coordinate Tags",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Switch(
                            checked = showCoordinates,
                            onCheckedChange = { showCoordinates = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonMintDark,
                                checkedTrackColor = NeonMint,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ObsidianSurfaceHigh
                            )
                        )
                    }
                }
            }

            // Quick Target Inspector Tray
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Configured Coordinates",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${reticles.size} Targets Active",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        reticles.forEach { reticle ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianSurfaceLow)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(reticle.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = reticle.id.toString(),
                                            color = ObsidianSurfaceDim,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reticle.label,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = "X: ${reticle.x.toInt() * 4} • Y: ${reticle.y.toInt() * 4}",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
