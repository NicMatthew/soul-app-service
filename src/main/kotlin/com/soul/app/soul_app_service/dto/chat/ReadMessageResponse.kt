package com.soul.app.soul_app_service.dto.chat

data class ReadMessageResponse(
    val type: String = "READ_RECEIPT",
    val conversationId: Int,
    val readerUserId: Int,
    val readAt: String
)
