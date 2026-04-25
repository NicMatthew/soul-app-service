package com.soul.app.soul_app_service.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.soul.app.soul_app_service.dto.ChannelType
import com.soul.app.soul_app_service.model.Notification
import com.soul.app.soul_app_service.registry.OnlineUserRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class NotifWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val onlineUserRegistry: OnlineUserRegistry
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(NotifWebSocketHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Int
        if (userId == null) {
            log.warn("Notif WS rejected — no userId | sessionId={}", session.id)
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }

        onlineUserRegistry.register(userId, session, ChannelType.NOTIF)
        log.info("Notif WS connected | userId={} | sessionId={}", userId, session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes["userId"] as Int
        onlineUserRegistry.remove(userId,session)
        log.info("Notif WS disconnected | userId={} | sessionId={} | status={}", userId, session.id, status)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("Notif transport error | sessionId={} | error={}", session.id, exception.message)
    }

    fun sendToUser(userId: Int, notif: Notification) {
        val sessions = onlineUserRegistry.get(userId, ChannelType.NOTIF)

        if (sessions.isEmpty()) {
            log.debug("User not connected to notif WS | userId={}", userId)
            return
        }

        val json = objectMapper.writeValueAsString(notif)

        sessions.forEach { session ->
            if (session.isOpen) {
                runCatching {
                    session.sendMessage(TextMessage(json))
                    log.info("Notif sent | userId={} | sessionId={}", userId, session.id)
                }.onFailure {
                    log.error("Failed to send notif | userId={} | error={}", userId, it.message)
                }
            }
        }
    }
}