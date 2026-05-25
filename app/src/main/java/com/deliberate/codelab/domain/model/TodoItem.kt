package com.deliberate.codelab.domain.model

import java.util.UUID

enum class Priority { LOW, MEDIUM, HIGH, CRITICAL }
enum class Repetition { NONE, DAILY, WEEKLY, MONTHLY, CUSTOM }
enum class Status { PENDING, IN_PROGRESS, COMPLETED, SKIPPED }

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val repetition: Repetition = Repetition.NONE,
    val timeInMillis: Long? = null,
    val status: Status = Status.PENDING,
    val priority: Priority = Priority.MEDIUM
)