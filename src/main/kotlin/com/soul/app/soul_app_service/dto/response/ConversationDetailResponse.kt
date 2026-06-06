package com.soul.app.soul_app_service.dto.response

import com.soul.app.soul_app_service.dto.chat.ChatMessageResponse
import java.util.Date

data class ConversationDetailResponse(
    val startTime: String,
    val endTime: String,
    val appointmentDate: Date,
    val messages :List<ChatMessageResponse>
)
