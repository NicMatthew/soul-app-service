package com.soul.app.soul_app_service.dto.chat

data class ChatSendMessageRequest(
    val conversationId: Int,
    val senderId: Int,
    val content: String
)