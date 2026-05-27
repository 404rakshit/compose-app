package com.deliberate.quickalarm.domain.model // Use your actual package name

// Keep your existing Enums
enum class Status { PENDING, COMPLETED }
enum class Repetition { NONE, DAILY, WEEKLY, MONTHLY }
enum class Priority { LOW, MEDIUM, HIGH }

data class TodoItem(
    // 1. Primary Identifiers (ID is now a String to support UUIDs)
    val id: String = "",
    val title: String,
    val status: Status = Status.PENDING,

    // 2. New Habit Tracking Fields
    val type: String = "Simple",
    val icon: String = "✨",
    val colorArgb: Int = 0,
    val repeatGoal: String = "Daily",
    val category: String = "Health",
    val reminders: String = "",

    // 3. Legacy / Advanced Fields (Optional)
    val description: String? = null,
    val repetition: Repetition? = Repetition.DAILY,
    val timeInMillis: Long? = 0L,
    val priority: Priority? = Priority.LOW
)