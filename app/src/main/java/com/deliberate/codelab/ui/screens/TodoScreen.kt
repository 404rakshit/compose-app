package com.deliberate.codelab.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deliberate.codelab.TodoViewModel
import com.deliberate.codelab.domain.model.Status
import com.deliberate.codelab.domain.model.TodoItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.deliberate.codelab.ui.components.HabitContributionGraph
import java.time.LocalDate
import androidx.compose.ui.graphics.Brush

@Composable
fun TodoScreen(viewModel: TodoViewModel, onAddHabitClick: () -> Unit) {
    // 1. USE REAL DATA FROM VIEWMODEL
    val todos = viewModel.todos

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabitClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Routine")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 48.dp, bottom = 100.dp)
        ) {
            item { HeaderSection() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { WeeklyCalendar() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            // item { ReminderBanner() }
//            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daily routine",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "See all",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 2. SHOW EMPTY STATE IF NO HABITS
            if (todos.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text("No habits yet. Click + to start!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 3. RENDER REAL LIST
            itemsIndexed(todos) { index, todo ->
                RoutineTimelineItem(
                    todo = todo,
                    isLastItem = index == todos.lastIndex,
                    onToggle = { viewModel.toggleTodo(todo.id) },

                    // ADD THIS LINE: Pass the real history to the graph!
                    completedDates = todo.completedDates
                )
            }
        }
    }
}

@Composable
fun HeaderSection() {
    // Dynamically format today's date (e.g., "Thursday, 10 March, 2025")
    val currentDate = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.getDefault()).format(Date())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Morning, Budi", // We can make the username dynamic later!
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🐯", fontSize = 32.sp)
            }
        }
    }
}

@Composable
fun WeeklyCalendar() {
    // 1. Define formatters for the Day (e.g., "Mon") and Date (e.g., "7")
    val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("d", Locale.getDefault())

    // 2. Identify today's date string so we can highlight it
    val today = Calendar.getInstance()
    val todayDateString = dateFormatter.format(today.time)

    // 3. Generate a rolling 7-day week (3 days past, today, 3 days future)
    val days = remember {
        val calendar = Calendar.getInstance()
        // Move the calendar back by 3 days to start our rolling window
        calendar.add(Calendar.DAY_OF_YEAR, -3)

        // Map over 7 days to generate the pairs of (DayName to DateNumber)
        (0..6).map {
            val dayName = dayFormatter.format(calendar.time)
            val dateNum = dateFormatter.format(calendar.time)

            // Advance the calendar by 1 day for the next iteration
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            dayName to dateNum
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days.size) { index ->
            val (dayName, date) = days[index]
            val isSelected = date == todayDateString

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderBanner() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Set the reminder",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Never miss your morning routine!\nSet a reminder to stay on track",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Set Now", fontWeight = FontWeight.Bold)
                }
            }

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Reminder",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun RoutineTimelineItem(
    todo: TodoItem,
    isLastItem: Boolean,
    onToggle: () -> Unit,
    completedDates: Set<LocalDate> = emptySet()
) {
    val isCompleted = todo.status == Status.COMPLETED
    val customColor = if (todo.colorArgb != 0) Color(todo.colorArgb) else MaterialTheme.colorScheme.secondary

    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // --- GRADIENT BACKGROUND APPLIED HERE ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            customColor.copy(alpha = 0.2f), // Starts with a 20% tint of the user's color
                            Color.Transparent               // Fades cleanly into the default surface color
                        )
                    )
                )
        ) {

            // --- TOP SECTION: Icon, Details, and the Top-Right Checkmark ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {

                // 1. DYNAMIC ICON Background
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    // Upped the alpha slightly to 0.25f so the icon block still pops against the new gradient!
                    color = customColor.copy(alpha = 0.25f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = todo.icon, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 2. MIDDLE DETAILS (Title & Desc)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!todo.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = "Streak 3 days",
//                            style = MaterialTheme.typography.labelMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )

                        if (todo.priority != null) {
//                            Text(" • ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${todo.priority.name} PRIORITY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 3. TOP-RIGHT CHECKMARK & REMINDERS
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = "Completed", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Mark Complete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!todo.reminders.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = "Time", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(todo.reminders.split(",").first(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Text(todo.repeatGoal, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // --- BOTTOM SECTION: The Heatmap Graph ---
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

//                HabitContributionGraph(
//                    completedDates = completedDates,
//                    habitColor = customColor, // PASS THE COLOR HERE
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
            }
        }
    }
}