//package com.soul.app.soul_app_service.config
//
//import com.soul.app.soul_app_service.service.JwtService
//import org.springframework.context.annotation.Configuration
//import org.springframework.messaging.Message
//import org.springframework.messaging.MessageChannel
//import org.springframework.messaging.simp.config.ChannelRegistration
//import org.springframework.messaging.simp.stomp.StompCommand
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor
//import org.springframework.messaging.support.ChannelInterceptor
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
//import org.springframework.security.core.authority.SimpleGrantedAuthority
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
//
//@Configuration
//class WebSocketSecurityConfig(
//    private val jwtService: JwtService
//) : WebSocketMessageBrokerConfigurer {
//
//    override fun configureClientInboundChannel(registration: ChannelRegistration) {
//        registration.interceptors(object : ChannelInterceptor {
//            override fun preSend(
//                message: Message<*>,
//                channel: MessageChannel
//            ): Message<*> {
//
//                val accessor = StompHeaderAccessor.wrap(message)
//
//                if (StompCommand.CONNECT == accessor.command) {
//                    val token = accessor.getFirstNativeHeader("Authorization")
//                        ?.removePrefix("Bearer ")
//
//                    if (token != null) {
//                        val decoded = jwtService.decode(token)
//                        accessor.user = UsernamePasswordAuthenticationToken(
//                            decoded.subject,
//                            null,
//                            listOf(SimpleGrantedAuthority("ROLE_${decoded.getClaim("role").asString()}"))
//                        )
//                    }
//                }
//
//                return message
//            }
//        })
//    }
//}