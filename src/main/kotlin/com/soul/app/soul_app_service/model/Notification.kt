package com.soul.app.soul_app_service.model

import java.sql.Timestamp

data class Notification(
    val id: Int? = null,
    val userId: Int,
    val type: String,
    val title: String,
    val description: String,
    val redirectUrl: String,
    val isRead: Boolean? = false,
    val createdAt: Timestamp? = null,
)
