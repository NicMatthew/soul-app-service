package com.soul.app.soul_app_service.dto.response

import com.soul.app.soul_app_service.model.User

data class GetAllConversationResponse(
    val conversationId: Int,
    val user : User,
    val lastMessage : String
)
