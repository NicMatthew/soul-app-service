package com.soul.app.soul_app_service.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class NotifWebSocketHandler(
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(NotifWebSocketHandler::class.java)

    private val sessions = ConcurrentHashMap<Int, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Int
        if (userId == null) {
            log.warn("Notif WS rejected — no userId | sessionId={}", session.id)
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }
        sessions[userId] = session
        log.info("Notif WS connected | userId={} | sessionId={}", userId, session.id)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes["userId"] as? Int
        if (userId != null) sessions.remove(userId)
        log.info("Notif WS disconnected | userId={} | sessionId={} | status={}", userId, session.id, status)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("Notif transport error | sessionId={} | error={}", session.id, exception.message)
    }

    fun sendToUser(userId: Int, event: Any) {
        val session = sessions[userId]
        if (session == null) {
            log.debug("User not connected to notif WS | userId={}", userId)
            return
        }
        if (!session.isOpen) {
            log.warn("Notif session closed | userId={}", userId)
            sessions.remove(userId)
            return
        }
        runCatching {
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(event)))
            log.info("Notif sent | userId={}", userId)
        }.onFailure {
            log.error("Failed to send notif | userId={} | error={}", userId, it.message)
        }
    }

    fun sendToUsers(userIds: List<Int>, event: Any) {
        userIds.forEach { sendToUser(it, event) }
    }

    fun broadcast(event: Any) {
        val json = objectMapper.writeValueAsString(event)
        sessions.forEach { (userId, session) ->
            if (session.isOpen) {
                runCatching { session.sendMessage(TextMessage(json)) }
                    .onFailure { log.warn("Broadcast failed | userId={}", userId) }
            }
        }
    }
}