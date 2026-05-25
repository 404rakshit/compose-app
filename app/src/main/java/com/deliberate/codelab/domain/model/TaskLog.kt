package com.deliberate.codelab.domain.model

import java.util.UUID

data class TaskLog(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val completedAt: Long
)