package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.chat.ChatMessageResponse
import com.soul.app.soul_app_service.dto.response.GetAllConversationResponse
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.service.ChatService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/chat")
@Tag(
    name = "Chat Controller",
)
class ChatController(private val chatService: ChatService) {
    @GetMapping("/conversations")
    @Operation(
        summary = "Get all User Conversations",
    )
    fun getAllConversation(): ResponseEntity<List<GetAllConversationResponse>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(chatService.getAllConversation(userId))
    }
    @GetMapping("/conversations/{conversationId}")
    @Operation(
        summary = "Get Conversation Messages",
    )
    fun getConversationMessages(
        @PathVariable conversationId: Int
    ): ResponseEntity<List<ChatMessageResponse>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(chatService.getConversationMessages(conversationId, userId))
    }
}