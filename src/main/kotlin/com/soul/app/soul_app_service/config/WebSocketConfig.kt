package com.soul.app.soul_app_service.config

import com.soul.app.soul_app_service.handler.ChatWebSocketHandler
import com.soul.app.soul_app_service.handler.NotifWebSocketHandler
import com.soul.app.soul_app_service.interceptor.JwtHandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val chatHandler: ChatWebSocketHandler,
    private val notifHandler: NotifWebSocketHandler,
    private val jwtHandshakeInterceptor: JwtHandshakeInterceptor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatHandler, "/ws")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOriginPatterns("*")

        registry.addHandler(notifHandler, "/ws/notif")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOriginPatterns("*")
    }
}
