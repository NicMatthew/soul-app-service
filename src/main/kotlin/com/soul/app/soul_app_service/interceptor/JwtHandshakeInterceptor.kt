package com.soul.app.soul_app_service.interceptor

import com.soul.app.soul_app_service.service.JwtService
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
@Component
class JwtHandshakeInterceptor(
    private val jwtService: JwtService
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {

        val token = request.headers.getFirst("Authorization")
            ?.removePrefix("Bearer ")

        if (token != null) {
            val decoded = jwtService.decode(token)
            attributes["userId"] = decoded.subject.toInt()
            attributes["role"] = decoded.getClaim("role").asString()
            return true
        }

        return false
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {}
}
