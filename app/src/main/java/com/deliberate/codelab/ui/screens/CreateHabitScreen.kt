package com.deliberate.codelab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

// A simple data class to hold our state before saving
data class HabitDraft(
    var type: String = "Simple", // "Simple" or "Unit"
    var name: String = "",
    var icon: String = "✨",
    var color: Color = Color(0xFFF07C27), // Default Orange
    var repeatGoal: String = "Daily",
    var category: String = "Health",
    var reminders: List<String> = emptyList() // E.g., ["08:00 AM", "09:00 PM"]
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    onBack: () -> Unit,
    onSave: (HabitDraft) -> Unit
) {
    // 1. Master State
    var draft by remember { mutableStateOf(HabitDraft()) }

    // 2. Pager State
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Dynamic Title based on page
                    val title = when(pagerState.currentPage) {
                        0 -> "Habit Type"
                        1 -> "Customize"
                        2 -> "Tracking Goal"
                        else -> "Reminders"
                    }
                    Text(title, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            val isLastPage = pagerState.currentPage == 3
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .navigationBarsPadding() // <-- 1. ADD THIS RIGHT HERE!
                    .padding(24.dp)          // 2. Keep your visual padding after it
            ) {
                Button(
                    onClick = {
                        if (isLastPage) {
                            onSave(draft)
                        } else {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    // Optional: Disable "Next" on page 1 if name is empty
                    enabled = if (pagerState.currentPage == 1) draft.name.isNotBlank() else true
                ) {
                    Text(if (isLastPage) "Save Habit" else "Next", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(65.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            // Disable user swiping so they MUST use the next button (optional, but good for forms)
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> Step1HabitType(
                    selectedType = draft.type,
                    onSelect = { draft = draft.copy(type = it) }
                )
                1 -> Step2Customize(
                    name = draft.name,
                    onNameChange = { draft = draft.copy(name = it) },
                    selectedIcon = draft.icon,
                    onIconSelect = { draft = draft.copy(icon = it) },
                    selectedColor = draft.color,
                    onColorSelect = { draft = draft.copy(color = it) }
                )
                2 -> Step3Tracking(
                    repeatGoal = draft.repeatGoal,
                    onRepeatSelect = { draft = draft.copy(repeatGoal = it) },
                    category = draft.category,
                    onCategorySelect = { draft = draft.copy(category = it) }
                )
                3 -> Step4Reminders(
                    reminders = draft.reminders,
                    onAddReminder = { timeString ->
                        // Automatically adds the newly formatted time to the list
                        draft = draft.copy(reminders = draft.reminders + timeString)
                    },
                    onRemoveReminder = { time ->
                        draft = draft.copy(reminders = draft.reminders - time)
                    }
                )
            }
        }
    }
}

// ==========================================
// STEP 1: TYPE
// ==========================================
@Composable
fun Step1HabitType(selectedType: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("What kind of habit do you want to build?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        TypeCard(
            title = "Simple Check",
            description = "Yes or No. Did you meditate today? Did you read?",
            icon = "✅",
            isSelected = selectedType == "Simple",
            onClick = { onSelect("Simple") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TypeCard(
            title = "Unit Trackable",
            description = "Track an exact amount. Drink 3 liters of water. Walk 10,000 steps.",
            icon = "📊",
            isSelected = selectedType == "Unit",
            onClick = { onSelect("Unit") }
        )
    }
}

@Composable
fun TypeCard(title: String, description: String, icon: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ==========================================
// STEP 2: CUSTOMIZE
// ==========================================
@Composable
fun Step2Customize(
    name: String, onNameChange: (String) -> Unit,
    selectedIcon: String, onIconSelect: (String) -> Unit,
    selectedColor: Color, onColorSelect: (Color) -> Unit
) {
    val emojis = listOf("🏃", "💧", "🧘", "📚", "💊", "💪", "🍎", "💤", "🧹", "✨")
    val colors = listOf(
        Color(0xFFF07C27), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107)  // Yellow
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Habit Name") },
            placeholder = { Text("e.g., Morning Run") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text("Choose an Icon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(emojis) { emoji ->
                val isSelected = emoji == selectedIcon
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(56.dp).clickable { onIconSelect(emoji) }
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Theme Color", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(colors) { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelect(color) }
                        .border(4.dp, if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent, CircleShape)
                )
            }
        }
    }
}

// ==========================================
// STEP 3: TRACKING GOAL
// ==========================================
@Composable
fun Step3Tracking(
    repeatGoal: String, onRepeatSelect: (String) -> Unit,
    category: String, onCategorySelect: (String) -> Unit
) {
    val frequencies = listOf("Daily", "Weekly", "Specific Days")
    val categories = listOf("Health", "Focus", "Chores", "Learning", "Mindfulness")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("How often do you want to do this?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            frequencies.forEach { freq ->
                FilterChip(
                    selected = repeatGoal == freq,
                    onClick = { onRepeatSelect(freq) },
                    label = { Text(freq) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Note: Accommodating wrapped chips without Accompanist requires a bit of layout logic,
        // so we'll use a LazyRow for simplicity here. In a production app, consider FlowRow.
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { onCategorySelect(cat) },
                    label = { Text(cat) }
                )
            }
        }
    }
}

// ==========================================
// STEP 4: REMINDERS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4Reminders(
    reminders: List<String>,
    onAddReminder: (String) -> Unit, // Updated to accept the formatted time string
    onRemoveReminder: (String) -> Unit
) {
    // State to show/hide the popup
    var showTimePicker by remember { mutableStateOf(false) }
    // State to hold the selected hour/minute
    val timePickerState = rememberTimePickerState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "Set Daily Reminders",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We will send you a push notification. You can add multiple alerts, or leave this blank to skip.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No reminders set", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            reminders.forEach { time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { onRemoveReminder(time) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { showTimePicker = true }, // Triggers the Dialog
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Alert Time")
        }
    }

    // ==========================================
    // THE TIME PICKER DIALOG
    // ==========================================
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                // The actual physical clock UI
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    // Formatting the 24-hour state into a readable 12-hour AM/PM string
                    val isAm = timePickerState.hour < 12
                    val displayHour = if (timePickerState.hour % 12 == 0) 12 else timePickerState.hour % 12
                    val displayMinute = timePickerState.minute.toString().padStart(2, '0')
                    val amPm = if (isAm) "AM" else "PM"

                    val formattedTime = "$displayHour:$displayMinute $amPm"

                    // Pass it back up to the master state!
                    onAddReminder(formattedTime)
                    showTimePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}