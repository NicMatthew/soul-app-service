package com.soul.app.soul_app_service.dto.chat

data class ChatMessageResponse(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val content: String,
    val createdAt: String,
    val isRead: Boolean = false,
    val readAt: String? = null
)