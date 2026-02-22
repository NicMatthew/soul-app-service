package com.soul.app.soul_app_service.model

import java.time.LocalDateTime

data class Conversation(
    val id: Int,
    val appointmentId: Int,
    val createdAt: LocalDateTime,
    val lastMessageAt: LocalDateTime?
)
