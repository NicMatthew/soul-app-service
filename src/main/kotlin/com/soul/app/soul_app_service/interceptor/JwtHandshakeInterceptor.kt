package com.soul.app.soul_app_service.interceptor

import com.soul.app.soul_app_service.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class JwtHandshakeInterceptor(
    private val jwtService: JwtService
) : HandshakeInterceptor {

    private val log = LoggerFactory.getLogger(JwtHandshakeInterceptor::class.java)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val token = request.uri.query
            ?.split("&")
            ?.firstOrNull { it.startsWith("token=") }
            ?.removePrefix("token=")

        if (token == null) {
            log.warn("WS connection rejected — no token provided")
            return false
        }

        val userId = runCatching {
            jwtService.decode(token).subject.toInt()
        }.getOrElse {
            log.warn("WS connection rejected — invalid/expired token | error={}", it.message)
            return false
        }

        attributes["userId"] = userId
        log.info("WS handshake success | userId={}", userId)
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {}
}