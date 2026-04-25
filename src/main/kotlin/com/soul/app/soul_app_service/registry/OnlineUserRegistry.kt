package com.soul.app.soul_app_service.registry

import com.soul.app.soul_app_service.dto.ChannelType
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class OnlineUserRegistry(private val userRepository: UserRepository) {

    private val sessions = ConcurrentHashMap<Int, MutableMap<ChannelType, MutableSet<WebSocketSession>>>()

    fun register(userId: Int, session: WebSocketSession, type: ChannelType) {
        userRepository.updateUserOnlineStatus(userId,true)
        sessions
            .computeIfAbsent(userId) { ConcurrentHashMap() }
            .computeIfAbsent(type) { ConcurrentHashMap.newKeySet() }
            .add(session)
    }

    fun get(userId: Int, type: ChannelType): Set<WebSocketSession> =
        sessions[userId]?.get(type) ?: emptySet()

    fun remove(userId:Int,session: WebSocketSession) {
        userRepository.updateUserOnlineStatus(userId,false)
        sessions.forEach { (_, typeMap) ->
            typeMap.values.forEach { it.remove(session) }
        }
    }
    fun getSessionsForUsers(userIds: List<Int>, type: ChannelType? = null): List<WebSocketSession> {
        return userIds.flatMap { userId ->
            val userSessions = sessions[userId] ?: return@flatMap emptyList()

            if (type != null) {
                userSessions[type]?.toList() ?: emptyList()
            } else {
                userSessions.values.flatten()
            }
        }
    }
}