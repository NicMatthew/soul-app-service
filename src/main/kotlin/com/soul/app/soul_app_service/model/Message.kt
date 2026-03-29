package com.soul.app.soul_app_service.model

import java.time.LocalDateTime

data class Message(
    val id: Int,
    val conversationId: Int,
    val senderUserId: Int,
    val messageText: String,
    val sentAt: LocalDateTime,
    val isRead: Boolean = false,
    val readAt: LocalDateTime? = null
)