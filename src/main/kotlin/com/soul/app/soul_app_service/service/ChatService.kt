package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.chat.ChatMessageResponse
import com.soul.app.soul_app_service.dto.chat.ChatSendMessageRequest
import com.soul.app.soul_app_service.model.Message
import com.soul.app.soul_app_service.repository.ChatRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
@Service
class ChatService(
    private val chatRepository: ChatRepository
) {

    fun sendMessage(req: ChatSendMessageRequest): ChatMessageResponse {

        if (req.content.isBlank()) {
            throw IllegalArgumentException("Message content cannot be empty")
        }

        val conversation = chatRepository.getConversationById(req.conversationId)
            ?: throw IllegalArgumentException("Conversation not found")

        val participants = chatRepository.getConversationParticipants(req.conversationId)
            ?: throw IllegalArgumentException("Conversation participants not found")

        if (!participants.contains(req.senderId)) {
            throw IllegalArgumentException("Sender is not part of this conversation")
        }

        val now = LocalDateTime.now()

        val savedId = chatRepository.createMessage(
            Message(
                id = -99, // DB akan assign ID sebenarnya
                conversationId = conversation.id,
                senderUserId = req.senderId,
                messageText = req.content.trim(),
                sentAt = now
            )
        )

        // Return response, nanti handler yang broadcast
        return ChatMessageResponse(
            id = savedId,
            conversationId = conversation.id,
            senderId = req.senderId,
            content = req.content.trim(),
            createdAt = now.toString()
        )
    }
}