package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AutoClickAccessibilityService
import com.example.ui.theme.CriticalCoral
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.NeonMint
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

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var hapticFeedback by remember { mutableStateOf(true) }
    var soundFeedback by remember { mutableStateOf(false) }
    var gaussianAntiBan by remember { mutableStateOf(true) }
    var jitterVariancePx by remember { mutableFloatStateOf(5f) }
    var highFrequencyMode by remember { mutableStateOf(true) }

    val hasOverlayPermission = Settings.canDrawOverlays(context)
    val hasAccessibility = AutoClickAccessibilityService.isAccessibilityEnabled(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Required Permissions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SYSTEM ENGINE PERMISSIONS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Overlay Permission Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = if (hasOverlayPermission) NeonMint else TriggerAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Display Over Other Apps",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasOverlayPermission) "Overlay permission granted" else "Required for floating HUD dock",
                                color = if (hasOverlayPermission) NeonMint else TriggerAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (hasOverlayPermission) NeonMint.copy(alpha = 0.2f) else ElectricCyan)
                            .clickable {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    context.startActivity(intent)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasOverlayPermission) "GRANTED" else "MANAGE",
                            color = if (hasOverlayPermission) NeonMint else ElectricCyanDark,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Accessibility Service Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceLow)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = if (hasAccessibility) NeonMint else TriggerAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Accessibility Tap Service",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasAccessibility) "Service hooked • 0.8ms latency" else "Tap to enable auto-clicking",
                                color = if (hasAccessibility) NeonMint else TriggerAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (hasAccessibility) NeonMint.copy(alpha = 0.2f) else ElectricCyan)
                            .clickable {
                                try {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (hasAccessibility) "ACTIVE" else "ENABLE",
                            color = if (hasAccessibility) NeonMint else ElectricCyanDark,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Anti-Ban & Randomization Engine
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = NeonMint, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Anti-Ban Randomization Engine",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gaussian Sub-Pixel Jitter", color = TextPrimary, fontSize = 13.sp)
                        Text("Simulates natural human fingertip contact area", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = gaussianAntiBan,
                        onCheckedChange = { gaussianAntiBan = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonMintDark,
                            checkedTrackColor = NeonMint,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = ObsidianSurfaceHigh
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Coordinate Variance Radius", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            "±${jitterVariancePx.toInt()} px",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = jitterVariancePx,
                        onValueChange = { jitterVariancePx = it },
                        valueRange = 1f..25f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = ObsidianSurfaceDim
                        )
                    )
                }
            }
        }

        // Tactile & Ergonomics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Feedback & Performance",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Haptic Click Vibration", color = TextPrimary, fontSize = 13.sp)
                    }
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = { hapticFeedback = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonMintDark,
                            checkedTrackColor = NeonMint
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audio Tap Cadence", color = TextPrimary, fontSize = 13.sp)
                    }
                    Switch(
                        checked = soundFeedback,
                        onCheckedChange = { soundFeedback = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonMintDark,
                            checkedTrackColor = NeonMint
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Kernel Microsecond Bypass", color = TextPrimary, fontSize = 13.sp)
                            Text("Enables up to 100+ clicks/second", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = highFrequencyMode,
                        onCheckedChange = { highFrequencyMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonMintDark,
                            checkedTrackColor = NeonMint
                        )
                    )
                }
            }
        }

        // About & Version Card
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
                    Column {
                        Text("ClickForge Precision Engine", color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Version 2.4.0 (Build 2026.10)", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceHigh)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("PRO", color = NeonMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Professional Android screen automation with floating overlay reticles, multi-target macro pipelines, sub-pixel jitter anti-detection, and sub-millisecond execution.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
