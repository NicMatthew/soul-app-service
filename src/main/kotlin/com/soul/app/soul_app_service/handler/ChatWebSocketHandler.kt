package com.soul.app.soul_app_service.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.soul.app.soul_app_service.dto.chat.ChatSendMessageRequest
import com.soul.app.soul_app_service.service.ChatService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.java

@Component
class ChatWebSocketHandler(
    private val chatService: ChatService,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    // Map conversationId -> set of sessions
    private val conversations = ConcurrentHashMap<Int, MutableSet<WebSocketSession>>()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Parse JSON message
        val req = objectMapper.readValue(message.payload, ChatSendMessageRequest::class.java)
        val conversationId = req.conversationId

        // Simpan session ke conversation
        conversations.computeIfAbsent(conversationId) { ConcurrentHashMap.newKeySet() }.add(session)

        // Proses message
        val response = chatService.sendMessage(req)
        val json = objectMapper.writeValueAsString(response)

        // Broadcast ke semua peserta conversation
        conversations[conversationId]?.forEach { ws ->
            if (ws.isOpen) ws.sendMessage(TextMessage(json))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        // Remove session dari semua conversation
        conversations.values.forEach { it.remove(session) }
    }
}