package com.soul.app.soul_app_service.registry

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class OnlineUserRegistry {

    private val onlineUsers = ConcurrentHashMap<Int, MutableSet<WebSocketSession>>()

    fun register(userId: Int, session: WebSocketSession) {
        onlineUsers.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(session)
    }

    fun remove(session: WebSocketSession) {
        onlineUsers.values.forEach { it.remove(session) }
        onlineUsers.entries.removeIf { it.value.isEmpty() }
    }

    fun getSessionsForUsers(userIds: List<Int>): List<WebSocketSession> =
        userIds.flatMap { onlineUsers[it] ?: emptySet() }

    fun getOnlineUserIds(fromUserIds: List<Int>): List<Int> =
        fromUserIds.filter { onlineUsers.containsKey(it) }
}