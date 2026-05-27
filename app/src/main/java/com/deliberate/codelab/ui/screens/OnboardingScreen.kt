package com.deliberate.codelab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinishOnboarding: (String) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("English") }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(24.dp)// <-- ADD THIS HERE!
        ) {

            // ==========================================
            // TOP NAVIGATION BAR
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, start = 8.dp, end = 8.dp)
                    .height(48.dp)
            ) {
                val showBackButton = pagerState.currentPage > 0

                // 1. Keep the button in the tree, but control visibility via alpha
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .alpha(if (showBackButton) 1f else 0f), // Visually hide when on page 0
                    enabled = showBackButton // Prevent the user from clicking the invisible button
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { iteration ->
                        val isActive = pagerState.currentPage == iteration
                        val color = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }

                        // 2. Wrap each dot in a fixed 12.dp Box so their expansion doesn't push neighbors
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(12.dp), // The container size never changes
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 12.dp else 8.dp) // The physical dot expands securely inside
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // THE PAGER CONTENT
            // ==========================================
            // Modifier.weight(1f) pushes the fixed button below down to the bottom
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> LanguageSelectionPage(
                        currentLanguage = selectedLanguage,
                        onLanguageChange = { selectedLanguage = it }
                    )
                    1 -> ProgressTrackingPage()
                    2 -> FeatureListingPage()
                }
            }

            // ==========================================
            // NEW: FIXED BOTTOM BUTTON
            // ==========================================
            val isLastPage = pagerState.currentPage == 2

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinishOnboarding(selectedLanguage)
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                // Button text changes dynamically based on the current page
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // The extra space added beneath the button
            // Spacer(modifier = Modifier.height(65.dp))
        }
    }
}

// ==========================================
// PAGE 1: LANGUAGE SELECTION
// ==========================================
@Composable
fun LanguageSelectionPage(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val languages = listOf("English", "Spanish", "French", "German", "Hindi")

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Please select your language to continue.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // We use weight(1f) here so the list scales appropriately without the bottom button
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            languages.forEachIndexed { index, language ->
                val isSelected = (language == currentLanguage)
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    languages.lastIndex -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    else -> RectangleShape
                }
                val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index != languages.lastIndex) 1.dp else 0.dp)
                        .background(color = backgroundColor, shape = shape)
                        .clip(shape)
                        .clickable { onLanguageChange(language) }
                        .padding(vertical = 7.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onLanguageChange(language) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = language,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// PAGE 2: PROGRESS TRACKING
// ==========================================
@Composable
fun ProgressTrackingPage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Streak",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Build a Habit", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Keep track of your daily tasks and watch your streak grow. Consistency is the key to success.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// PAGE 3: FEATURE LISTING
// ==========================================
@Composable
fun FeatureListingPage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Everything You Need", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            FeatureRow(icon = Icons.Default.CheckCircle, title = "Task Management", desc = "Organize your life easily.")
            FeatureRow(icon = Icons.Default.Notifications, title = "Exact Alarms", desc = "Never miss a scheduled event.")
            FeatureRow(icon = Icons.Default.Star, title = "Gamification", desc = "Stay motivated with daily streaks.")
        }
    }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(12.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}