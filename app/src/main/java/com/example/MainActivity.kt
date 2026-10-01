package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClickForgeBottomBar
import com.example.ui.components.ClickForgeTopBar
import com.example.ui.components.NavigationTab
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FloatingHudScreen
import com.example.ui.screens.MultiTargetScreen
import com.example.ui.screens.ScriptsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ClickForgeTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.NeonMint
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceCard
import com.example.ui.theme.ObsidianSurfaceDim
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianSurfaceHighest
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TriggerAmber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClickForgeTheme {
                ClickForgeApp()
            }
        }
    }
}

@Composable
fun ClickForgeApp() {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var isShowingFloatingHud by remember { mutableStateOf(false) }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedSpeedMode by remember { mutableStateOf("Hyper Turbo (100 Hz)") }

    if (isShowingFloatingHud) {
        BackHandler {
            isShowingFloatingHud = false
        }
        FloatingHudScreen(
            onBack = { isShowingFloatingHud = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                val subtitle = when (currentTab) {
                    NavigationTab.DASHBOARD -> "Dashboard"
                    NavigationTab.MULTI_TARGET -> "Multi Target"
                    NavigationTab.SCRIPTS -> "Scripts"
                    NavigationTab.SETTINGS -> "Settings"
                }
                ClickForgeTopBar(
                    subtitle = subtitle,
                    onBoltClick = { showSpeedDialog = true },
                    onProfileClick = { showProfileDialog = true }
                )
            },
            bottomBar = {
                ClickForgeBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            },
            containerColor = ObsidianSurface
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ObsidianSurface)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tabTransition"
                ) { tab ->
                    when (tab) {
                        NavigationTab.DASHBOARD -> DashboardScreen(
                            onNavigateToFloatingHud = { isShowingFloatingHud = true }
                        )
                        NavigationTab.MULTI_TARGET -> MultiTargetScreen(
                            onNavigateToFloatingHud = { isShowingFloatingHud = true }
                        )
                        NavigationTab.SCRIPTS -> ScriptsScreen(
                            onNavigateToFloatingHud = { isShowingFloatingHud = true }
                        )
                        NavigationTab.SETTINGS -> SettingsScreen()
                    }
                }
            }
        }
    }

    // Performance Speed Mode Dialog
    if (showSpeedDialog) {
        val speedModes = listOf(
            Pair("Standard Cadence (10 Hz)", "100ms interval for typical games & forms"),
            Pair("Hyper Turbo (100 Hz)", "10ms kernel dispatch for high-speed idle clickers"),
            Pair("Sub-Millisecond Sniper (200 Hz)", "5ms burst for flash sales & instant triggers")
        )

        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Performance Speed Mode", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    speedModes.forEach { (mode, desc) ->
                        val isSelected = selectedSpeedMode == mode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ObsidianSurfaceHigh else ObsidianSurfaceDim)
                                .clickable {
                                    selectedSpeedMode = mode
                                    Toast.makeText(context, "Activated $mode", Toast.LENGTH_SHORT).show()
                                    showSpeedDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode,
                                        color = if (isSelected) ElectricCyan else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = desc,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSpeedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Done", color = ElectricCyanDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ObsidianSurfaceCard
        )
    }

    // Profile & Pro Status Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = ElectricCyanDark, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("ClickForge Operator", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("PRO UNLOCKED", color = NeonMint, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Unlimited Floating Overlay Reticles\n• Sub-pixel Jitter Anti-Detection active\n• Macro Pipeline Engine v2.4 enabled\n• Real hardware Accessibility gesture dispatch",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showProfileDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("Close", color = ElectricCyanDark, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ObsidianSurfaceCard
        )
    }
}
