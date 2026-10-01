package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultData
import com.example.model.TargetActionType
import com.example.model.TargetPoint
import com.example.service.ClickEngineController
import com.example.service.FloatingOverlayService
import com.example.ui.theme.CriticalCoral
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonMintContainer
import com.example.ui.theme.NeonMintDark
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceCard
import com.example.ui.theme.ObsidianSurfaceDim
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianSurfaceHighest
import com.example.ui.theme.ObsidianSurfaceLow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TriggerAmber
import kotlinx.coroutines.launch

@Composable
fun MultiTargetScreen(
    onNavigateToFloatingHud: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pipeline = remember { mutableStateListOf(*DefaultData.defaultMultiTargetPipeline.toTypedArray()) }

    var infiniteLoop by remember { mutableStateOf(true) }
    var antiBanShift by remember { mutableStateOf(true) }
    val isRunning by ClickEngineController.isRunning.collectAsState()

    val testRunProgress = remember { Animatable(0f) }
    var isTestRunning by remember { mutableStateOf(false) }

    fun addPoint(type: TargetActionType) {
        val nextId = pipeline.size + 1
        val newPoint = when (type) {
            TargetActionType.TAP -> TargetPoint(
                id = nextId,
                name = "Tap Node $nextId",
                type = TargetActionType.TAP,
                x = 400f + (nextId * 30),
                y = 600f + (nextId * 40),
                delayMs = 300L,
                holdDurationMs = 25L
            )
            TargetActionType.SWIPE -> TargetPoint(
                id = nextId,
                name = "Swipe Node $nextId",
                type = TargetActionType.SWIPE,
                x = 500f,
                y = 800f,
                swipeEndX = 500f,
                swipeEndY = 400f,
                delayMs = 400L,
                swipeDurationMs = 350L
            )
            TargetActionType.WAIT -> TargetPoint(
                id = nextId,
                name = "Standby Pause $nextId",
                type = TargetActionType.WAIT,
                delayMs = 1500L
            )
            TargetActionType.BURST -> TargetPoint(
                id = nextId,
                name = "Burst Attack $nextId",
                type = TargetActionType.BURST,
                x = 540f,
                y = 960f,
                burstCount = 5,
                burstCadenceMs = 20L
            )
        }
        pipeline.add(newPoint)
        Toast.makeText(context, "Added ${newPoint.name}", Toast.LENGTH_SHORT).show()
    }

    fun activateHUD() {
        if (pipeline.isEmpty()) {
            Toast.makeText(context, "Pipeline is empty! Add points first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (Settings.canDrawOverlays(context)) {
            FloatingOverlayService.start(context)
            ClickEngineController.startMacroSequence(
                context = context,
                points = pipeline,
                infiniteLoop = infiniteLoop,
                antiBanShift = antiBanShift
            )
            Toast.makeText(context, "Multi-Target HUD Activated Over Screen", Toast.LENGTH_SHORT).show()
        } else {
            ClickEngineController.startMacroSequence(
                context = context,
                points = pipeline,
                infiniteLoop = infiniteLoop,
                antiBanShift = antiBanShift
            )
            onNavigateToFloatingHud()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Macro Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceLow)
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Multi-Target Macro Sequence",
                            color = ElectricCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianSurfaceHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ENGINE V2.4",
                            color = NeonMint,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TriggerAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cycle Duration: ",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "4.8s",
                            color = TriggerAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Waypoints: ",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${pipeline.size} Nodes",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceCard)
                    .clickable { addPoint(TargetActionType.TAP) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Tap",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "+ Tap Point",
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceCard)
                    .clickable { addPoint(TargetActionType.SWIPE) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Swipe,
                        contentDescription = "Swipe",
                        tint = NeonMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "+ Swipe Flow",
                        color = NeonMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceCard)
                    .clickable { addPoint(TargetActionType.WAIT) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Wait",
                        tint = TriggerAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "+ Wait Delay",
                        color = TriggerAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Sub-Pixel Target Trace Visualizer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceDim)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SUB-PIXEL TARGET TRACE",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isTestRunning) ElectricCyan else ObsidianSurfaceHigh)
                            .clickable {
                                if (!isTestRunning) {
                                    isTestRunning = true
                                    coroutineScope.launch {
                                        testRunProgress.snapTo(0f)
                                        testRunProgress.animateTo(1f, animationSpec = tween(1400, easing = FastOutSlowInEasing))
                                        isTestRunning = false
                                    }
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Test Run",
                                tint = if (isTestRunning) ElectricCyanDark else ElectricCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Test Run",
                                color = if (isTestRunning) ElectricCyanDark else ElectricCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Track canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceHigh.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            val w = size.width
                            val h = size.height / 2f
                            moveTo(20f, h)
                            cubicTo(w * 0.25f, h - 25f, w * 0.45f, h + 25f, w * 0.55f, h)
                            cubicTo(w * 0.7f, h - 25f, w * 0.85f, h + 20f, w - 20f, h)
                        }
                        // Base dashed track
                        drawPath(
                            path = path,
                            color = Color(0xFF31353C),
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                        // Active test progress track
                        if (testRunProgress.value > 0f) {
                            drawPath(
                                path = path,
                                color = ElectricCyan,
                                style = Stroke(
                                    width = 4f,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(size.width * testRunProgress.value, size.width),
                                        0f
                                    )
                                )
                            )
                        }
                    }

                    // Node badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val nodes = listOf(
                            Triple("1", "Tap", ElectricCyan),
                            Triple("2", "Swipe", NeonMint),
                            Triple("3", "Tap", ElectricCyan),
                            Triple("4", "Wait", TextMuted),
                            Triple("5", "x5", TriggerAmber)
                        )

                        nodes.forEach { (num, label, color) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = num,
                                        color = if (color == TextMuted) TextPrimary else ElectricCyanDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = label,
                                    color = color,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sequence Pipeline Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sequence Pipeline",
                    color = ElectricCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        pipeline.reverse()
                        Toast.makeText(context, "Sequence Inverted", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Reorder,
                    contentDescription = "Sort",
                    tint = TextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SORT",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Pipeline List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pipeline.forEachIndexed { index, point ->
                PipelineItemCard(
                    point = point,
                    onDelete = {
                        pipeline.removeAt(index)
                    }
                )
            }
        }

        // Execution Modifiers Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceLow)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "EXECUTION MODIFIERS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Infinite Loop Cycle",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Continuous restart until stop trigger",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = infiniteLoop,
                        onCheckedChange = { infiniteLoop = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonMintDark,
                            checkedTrackColor = NeonMint,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = ObsidianSurfaceHigh
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Anti-Ban Sub-Pixel Shift",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ObsidianSurfaceHigh)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "±5px",
                                    color = NeonMint,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Gaussian coordinate jitter on each tap",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = antiBanShift,
                        onCheckedChange = { antiBanShift = it },
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

        // Primary Action Buttons
        Button(
            onClick = { activateHUD() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("activate_hud_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = ElectricCyanDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Activate Multi-Target HUD",
                    color = ElectricCyanDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = {
                Toast.makeText(context, "Saved as preset profile!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("save_preset_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceHigh)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save as Preset Profile",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PipelineItemCard(
    point: TargetPoint,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val (badgeColor, textColor) = when (point.type) {
        TargetActionType.TAP -> Pair(ElectricCyan, ElectricCyan)
        TargetActionType.SWIPE -> Pair(NeonMint, NeonMint)
        TargetActionType.WAIT -> Pair(ObsidianSurfaceHighest, TriggerAmber)
        TargetActionType.BURST -> Pair(TriggerAmber, TriggerAmber)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                if (point.type == TargetActionType.WAIT) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = TriggerAmber,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (point.type == TargetActionType.BURST) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = ObsidianSurfaceDim,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = point.id.toString(),
                        color = ObsidianSurfaceDim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = point.name,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianSurfaceHighest)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        val coordinateLabel = when (point.type) {
                            TargetActionType.TAP -> "X:${point.x.toInt()} Y:${point.y.toInt()}"
                            TargetActionType.SWIPE -> "${point.swipeDurationMs}ms flick"
                            TargetActionType.WAIT -> "${point.delayMs}ms"
                            TargetActionType.BURST -> "${point.burstCount}x QuickTap"
                        }
                        Text(
                            text = coordinateLabel,
                            color = textColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (point.type) {
                        TargetActionType.TAP -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timelapse, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${point.delayMs}ms delay", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TouchApp, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${point.holdDurationMs}ms hold", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        TargetActionType.SWIPE -> {
                            Text(
                                text = "500,800 → 500,300",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Linear curve",
                                color = NeonMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TargetActionType.WAIT -> {
                            Text(
                                text = "Screen idle state before next cycle trigger",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        TargetActionType.BURST -> {
                            Text(
                                text = "X:${point.x.toInt()} Y:${point.y.toInt()}",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${point.burstCadenceMs}ms cadence",
                                color = TriggerAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(ObsidianSurfaceHigh)
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete Target", color = CriticalCoral) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = CriticalCoral) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
