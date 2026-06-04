package com.deliberate.codelab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil

@Composable
fun HabitContributionGraph(
    completedDates: Set<LocalDate>,
    habitColor: Color, // 1. ADD THIS PARAMETER
    modifier: Modifier = Modifier,
    monthsToShow: Int = 4
) {
    val today = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.from(today) }

    val months = remember(currentMonth, monthsToShow) {
        (monthsToShow - 1 downTo 0).map { currentMonth.minusMonths(it.toLong()) }
    }

    // 2. DELETE the githubGreen variable
    val emptyBoxColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        months.forEach { yearMonth ->
            Column {
                val monthName = yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                Text(
                    text = monthName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val daysInMonth = yearMonth.lengthOfMonth()
                    val numColumns = ceil(daysInMonth / 7.0).toInt()

                    for (col in 0 until numColumns) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (row in 0..6) {
                                val dateInt = (col * 7) + row + 1

                                if (dateInt > daysInMonth) {
                                    Box(modifier = Modifier.size(12.dp))
                                } else {
                                    val currentDate = yearMonth.atDay(dateInt)
                                    val isCompleted = completedDates.contains(currentDate)
                                    val isFuture = currentDate.isAfter(today)

                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                when {
                                                    isFuture -> Color.Transparent
                                                    isCompleted -> habitColor // 3. USE IT HERE
                                                    else -> emptyBoxColor
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}