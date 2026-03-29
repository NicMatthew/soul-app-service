package com.soul.app.soul_app_service.dto.chat

data class OnlineUsersEvent(
    val type: String = "ONLINE_USERS",
    val onlineUserIds: Set<Int>
)