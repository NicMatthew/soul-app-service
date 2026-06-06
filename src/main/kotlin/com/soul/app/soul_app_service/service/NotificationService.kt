package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.NotificationType
import com.soul.app.soul_app_service.handler.NotifWebSocketHandler
import com.soul.app.soul_app_service.model.Notification
import com.soul.app.soul_app_service.publisher.NotificationPublisher
import com.soul.app.soul_app_service.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class NotificationService(
    private val notificationPublisher: NotificationPublisher,
    private val userRepository: UserRepository
) {

    fun sendNotification(userId: Int, notif: Notification) {
        userRepository.insertNotification(notif)
        notificationPublisher.push(userId, notif)
    }

}