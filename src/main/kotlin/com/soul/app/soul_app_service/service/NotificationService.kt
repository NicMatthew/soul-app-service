package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.NotificationType
import com.soul.app.soul_app_service.handler.NotifWebSocketHandler
import com.soul.app.soul_app_service.model.Notification
import com.soul.app.soul_app_service.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class NotificationService(
    private val notifHandler: NotifWebSocketHandler,
    private val userRepository: UserRepository

) {
    private val log = LoggerFactory.getLogger(NotificationService::class.java)
    fun sendNotification(userId: Int, notif: Notification) {
        log.info("Sending notification for user $userId with notif $notif")
        userRepository.insertNotification(notif)
        notifHandler.sendToUser(userId, notif)
    }


}