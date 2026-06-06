package com.soul.app.soul_app_service.publisher

import com.soul.app.soul_app_service.handler.NotifWebSocketHandler
import com.soul.app.soul_app_service.model.Notification
import org.springframework.stereotype.Component

@Component
class NotificationPublisher(
    private val notifHandler: NotifWebSocketHandler
) {
    fun push(userId: Int, notif: Notification) {
        notifHandler.sendToUser(userId, notif)
    }
}