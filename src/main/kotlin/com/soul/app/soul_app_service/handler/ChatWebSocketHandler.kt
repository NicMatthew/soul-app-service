package com.soul.app.soul_app_service.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.soul.app.soul_app_service.dto.ChannelType
import com.soul.app.soul_app_service.dto.chat.ChatSendMessageRequest
import com.soul.app.soul_app_service.registry.OnlineUserRegistry
import com.soul.app.soul_app_service.service.ChatService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ChatWebSocketHandler(
    private val chatService: ChatService,
    private val onlineUserRegistry: OnlineUserRegistry,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(ChatWebSocketHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Int
        if (userId == null) {
            log.warn("No userId in session attributes | sessionId={}", session.id)
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }
        onlineUserRegistry.register(userId, session, ChannelType.CHAT)
        log.info("User online | userId={} | sessionId={}", userId, session.id)
    }
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val req = runCatching {
            objectMapper.readValue(message.payload, ChatSendMessageRequest::class.java)
        }.getOrElse {
            log.error("Failed to parse | sessionId={} | error={}", session.id, it.message)
            return
        }

        when (req.type.uppercase()) {
            "MESSAGE" -> handleMessage(session, req)
            "READ"    -> handleRead(session, req)
            else -> log.warn("Unknown type | type={}", req.type)
        }
    }

    private fun handleMessage(session: WebSocketSession, req: ChatSendMessageRequest) {
        val response = runCatching { chatService.sendMessage(req) }.getOrElse {
            log.error("ChatService error | error={}", it.message)
            return
        }
        chatService.broadcastToOnlineMembers(req.conversationId, objectMapper.writeValueAsString(response))
    }

    private fun handleRead(session: WebSocketSession, req: ChatSendMessageRequest) {
        val userId = session.attributes["userId"] as? Int ?: return
        val event = chatService.markAsRead(req.conversationId, userId)

        chatService.broadcastToOnlineMembers(req.conversationId, objectMapper.writeValueAsString(event))
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("Transport error | sessionId={} | error={}", session.id, exception.message, exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.getUserId()
        if (userId == null) {
            log.warn("No userId in session attributes | sessionId={}", session.id)
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }
        onlineUserRegistry.remove(userId,session)
        log.info("User offline | userId={} | sessionId={} | status={}", userId, session.id, status)
    }

    private fun WebSocketSession.getUserId(): Int? =
        uri?.query
            ?.split("&")
            ?.firstOrNull { it.startsWith("userId=") }
            ?.removePrefix("userId=")
            ?.toIntOrNull()
}