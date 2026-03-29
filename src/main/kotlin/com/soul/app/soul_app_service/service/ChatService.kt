package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.chat.ChatMessageResponse
import com.soul.app.soul_app_service.dto.chat.ChatSendMessageRequest
import com.soul.app.soul_app_service.dto.chat.ReadMessageResponse
import com.soul.app.soul_app_service.dto.response.GetAllConversationResponse
import com.soul.app.soul_app_service.model.Conversation
import com.soul.app.soul_app_service.model.Message
import com.soul.app.soul_app_service.registry.OnlineUserRegistry
import com.soul.app.soul_app_service.repository.ChatRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import java.time.LocalDateTime
@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val onlineUserRegistry: OnlineUserRegistry,
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(ChatService::class.java)

    fun sendMessage(req: ChatSendMessageRequest): ChatMessageResponse {
        if (req.content.isBlank())
            throw IllegalArgumentException("Message content cannot be empty")

        val conversation = chatRepository.getConversationById(req.conversationId)
            ?: throw IllegalArgumentException("Conversation not found")

        val participants = chatRepository.getConversationParticipants(req.conversationId)
            ?: throw IllegalArgumentException("Conversation participants not found")

        if (!participants.contains(req.senderId))
            throw IllegalArgumentException("Sender is not part of this conversation")

        val now = LocalDateTime.now()
        val savedId = chatRepository.createMessage(
            Message(
                id = -99,
                conversationId = conversation.id,
                senderUserId = req.senderId,
                messageText = req.content.trim(),
                sentAt = now
            )
        )

        return ChatMessageResponse(
            id = savedId,
            conversationId = conversation.id,
            senderId = req.senderId,
            content = req.content.trim(),
            createdAt = now.toString()
        )
    }

    fun broadcastToOnlineMembers(conversationId: Int, json: String) {
        val participants = chatRepository.getConversationParticipants(conversationId) ?: return
        val sessions = onlineUserRegistry.getSessionsForUsers(participants)

        log.info("Broadcasting | conversationId={} | totalMembers={} | onlineSessions={}",
            conversationId, participants.size, sessions.size)

        val message = TextMessage(json)
        sessions.forEach { ws ->
            if (ws.isOpen) {
                runCatching { ws.sendMessage(message) }
                    .onFailure { log.warn("Failed to send | sessionId={} | error={}", ws.id, it.message) }
            }
        }
    }
    fun markAsRead(conversationId: Int, readerUserId: Int): ReadMessageResponse {
        val updatedCount = chatRepository.markMessagesAsRead(conversationId, readerUserId)
        log.info("Marked as read | conversationId={} | readerUserId={} | updatedRows={}",
            conversationId, readerUserId, updatedCount)

        return ReadMessageResponse(
            conversationId = conversationId,
            readerUserId = readerUserId,
            readAt = LocalDateTime.now().toString()
        )
    }
    fun getAllConversation(userId: Int): List<GetAllConversationResponse> {
        val conversations = chatRepository.getConversationsByUserId(userId)

        return conversations.mapNotNull { conversation ->
            val otherUserId = chatRepository.getOtherUserId(conversation.id, userId) ?: return@mapNotNull null
            val user = userRepository.getUserById(otherUserId) ?: return@mapNotNull null
            val lastMessage = chatRepository.getLastMessage(conversation.id)?.messageText ?: ""

            GetAllConversationResponse(
                conversationId = conversation.id,
                user = user,
                lastMessage = lastMessage
            )
        }
    }
    fun getConversationMessages(conversationId: Int, userId: Int): List<ChatMessageResponse> {
        val participants = chatRepository.getConversationParticipants(conversationId)
            ?: throw IllegalArgumentException("Conversation not found")

        if (!participants.contains(userId)) {
            throw IllegalArgumentException("Unauthorized")
        }

        return chatRepository.getMessagesByConversationId(conversationId).map { message ->
            ChatMessageResponse(
                id = message.id,
                conversationId = message.conversationId,
                senderId = message.senderUserId,
                content = message.messageText,
                createdAt = message.sentAt.toString(),
                isRead = message.isRead,
                readAt = message.readAt?.toString()
            )
        }
    }
}