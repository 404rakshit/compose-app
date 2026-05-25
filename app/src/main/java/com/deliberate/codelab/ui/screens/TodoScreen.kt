package com.deliberate.codelab.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    // Placeholder data
    val todos = remember {
        listOf(
            TodoItem(title = "Drink a glass of water", status = Status.COMPLETED),
            TodoItem(title = "Meditate to relax", status = Status.COMPLETED),
            TodoItem(title = "Stretch for 10 minutes", status = Status.PENDING),
            TodoItem(title = "Go for a short walk", status = Status.PENDING)
        )
    }

    Scaffold(
        // Use the native app background color
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Open your CreateTaskDialog */ },
                // Use native accent colors for the FAB
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
            item { ReminderBanner() }
            item { Spacer(modifier = Modifier.height(32.dp)) }

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

            itemsIndexed(todos) { index, todo ->
                RoutineTimelineItem(
                    todo = todo,
                    isLastItem = index == todos.lastIndex,
                    onToggle = { viewModel.toggleTodo(todo.id) }
                )
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Morning, Budi",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Thursday, 10 March, 2025",
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
    val days = listOf("Mon" to "7", "Tue" to "8", "Wed" to "9", "Thu" to "10", "Fri" to "11", "Sat" to "12", "Sun" to "13")
    val selectedDay = "10"

    LazyRow(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days.size) { index ->
            val (dayName, date) = days[index]
            val isSelected = date == selectedDay

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    // Use Primary color for active, transparent for inactive
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            // Invert text color based on background
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
        // Tertiary colors are great for promo/highlight banners in Material 3
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
fun RoutineTimelineItem(todo: TodoItem, isLastItem: Boolean, onToggle: () -> Unit) {
    val isCompleted = todo.status == Status.COMPLETED
    // Extract color for the dashed line to use inside the Canvas
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
    ) {
        // --- LEFT SIDE: THE TIMELINE ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                } else {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                }
            }

            if (!isLastItem) {
                Canvas(modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 4.dp)) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = outlineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 3f,
                        pathEffect = pathEffect
                    )
                }
            }
        }

        // --- RIGHT SIDE: THE TASK CARD ---
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            // Tie the card to the standard Surface color
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (isCompleted) "🥛" else "🧘", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Streak 3 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = "Time", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("5 min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}