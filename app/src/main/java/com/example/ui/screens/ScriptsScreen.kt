package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultData
import com.example.model.MacroProfile
import com.example.model.TargetActionType
import com.example.model.TargetPoint
import com.example.service.ClickEngineController
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

@Composable
fun ScriptsScreen(
    onNavigateToFloatingHud: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val profiles = remember { mutableStateListOf(*DefaultData.defaultProfiles.toTypedArray()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Gaming", "Productivity", "Social", "Shopping", "Idle Game")

    val filteredProfiles = profiles.filter { profile ->
        val matchesCategory = (selectedCategory == "All") || (profile.category.equals(selectedCategory, ignoreCase = true))
        val matchesQuery = searchQuery.isEmpty() ||
                profile.title.contains(searchQuery, ignoreCase = true) ||
                profile.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurfaceCard)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search macros, games, or apps...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scripts_search_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Voice recognition active", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Category Filter Pills (Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ElectricCyan else ObsidianSurfaceCard)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) ElectricCyanDark else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Quick Stats Bento Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurfaceCard)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MACROS",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${profiles.size}",
                                color = ElectricCyan,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "saved",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurfaceCard)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OUTPUT",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "184k",
                                color = NeonMint,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "taps",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurfaceCard)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BYPASS",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "99.9",
                                color = NeonMint,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "%",
                                color = NeonMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            // Automation Profiles Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AUTOMATION PROFILES",
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.08.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                profiles.reverse()
                                Toast.makeText(context, "Sorted profiles", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Sort",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "RECENTS",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Profiles synced to cloud backup", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Sync",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Script List Stack
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredProfiles.forEachIndexed { index, profile ->
                    ScriptCard(
                        profile = profile,
                        onExecute = {
                            ClickEngineController.startMacroSequence(
                                context = context,
                                points = if (profile.points.isNotEmpty()) profile.points else DefaultData.defaultMultiTargetPipeline
                            )
                            onNavigateToFloatingHud()
                        },
                        onDuplicate = {
                            val copy = profile.copy(
                                id = "copy_${System.currentTimeMillis()}",
                                title = "${profile.title} (Copy)"
                            )
                            profiles.add(copy)
                            Toast.makeText(context, "Profile duplicated", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            profiles.removeAt(index)
                            Toast.makeText(context, "Profile removed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Ambient Floating Action Launcher: "+ Create Macro"
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
                .testTag("create_macro_fab"),
            shape = RoundedCornerShape(24.dp),
            containerColor = ElectricCyan,
            contentColor = ElectricCyanDark
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Macro",
                    tint = ElectricCyanDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Create Macro",
                    color = ElectricCyanDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Create Macro Dialog
        if (showCreateDialog) {
            var newTitle by remember { mutableStateOf("") }
            var newCategory by remember { mutableStateOf("Gaming") }
            var newDelay by remember { mutableStateOf("100") }

            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = {
                    Text(
                        text = "New Macro Script",
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Macro Title", color = TextSecondary) },
                            placeholder = { Text("e.g. Daily Boss Quest Auto-Cycle") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = ObsidianSurfaceHighest,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = { newCategory = it },
                            label = { Text("Category", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = ObsidianSurfaceHighest,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newDelay,
                            onValueChange = { newDelay = it },
                            label = { Text("Cycle Delay (ms)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = ObsidianSurfaceHighest,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                profiles.add(
                                    MacroProfile(
                                        id = "custom_${System.currentTimeMillis()}",
                                        title = newTitle,
                                        category = newCategory,
                                        categoryTag = "CUSTOM",
                                        description = "User created custom precision macro pipeline.",
                                        targetsCount = 3,
                                        cycleDelayMs = newDelay.toLongOrNull() ?: 100L,
                                        lastRunText = "Just now",
                                        isActive = true
                                    )
                                )
                                showCreateDialog = false
                                Toast.makeText(context, "Macro created!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                    ) {
                        Text("Create", color = ElectricCyanDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = ObsidianSurfaceCard
            )
        }
    }
}

@Composable
fun ScriptCard(
    profile: MacroProfile,
    onExecute: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(profile.isActive) }
    val context = LocalContext.current

    val accentColor = when (profile.category.lowercase()) {
        "gaming" -> NeonMint
        "social" -> ElectricCyan
        "shopping" -> TriggerAmber
        else -> ElectricCyan
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianSurfaceHigh)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = profile.category.uppercase(),
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianSurfaceHighest)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = profile.categoryTag.uppercase(),
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonMintDark,
                            checkedTrackColor = NeonMint,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = ObsidianSurfaceHigh
                        )
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
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
                                text = { Text("Edit Script", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = ElectricCyan) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "Opening script editor...", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = TextMuted) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export JSON / QR", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.QrCode2, null, tint = TextMuted) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "Exported JSON to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share to Cloud", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Share, null, tint = NeonMint) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "Shared to ClickForge community library", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = CriticalCoral) },
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

            Spacer(modifier = Modifier.height(6.dp))

            // Main Info
            Text(
                text = profile.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = profile.description,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Parameters Strip
            if (profile.isSniper) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceDim)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = TriggerAmber,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Launch: 12:00:00.000 AM",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "5 ms BURST",
                            color = TriggerAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceLow)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (profile.category == "Social") "Swipe Pace" else "Targets",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (profile.category == "Social") "8.5 sec" else "${profile.targetsCount} pts",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ObsidianSurfaceLow)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (profile.category == "Social") "Human Variance" else "Cycle Delay",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (profile.category == "Social") "±350ms" else "${profile.cycleDelayMs} ms",
                                color = NeonMint,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profile.category == "Social") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = NeonMint,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Anti-Detect active",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Text(
                        text = if (profile.isSniper) profile.lastRunText else "Last run: ${profile.lastRunText}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onExecute,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (profile.isSniper) ObsidianSurfaceHighest else accentColor
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (profile.isSniper) Icons.Default.Timer else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (profile.isSniper) TextPrimary else ElectricCyanDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (profile.isSniper) "Arm Sniper" else "Execute",
                            color = if (profile.isSniper) TextPrimary else ElectricCyanDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
