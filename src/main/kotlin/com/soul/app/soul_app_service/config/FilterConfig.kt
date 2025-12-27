package com.soul.app.soul_app_service.config

import com.soul.app.soul_app_service.filter.JwtTokenAuthenticationFilter
import com.soul.app.soul_app_service.service.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig(
    private val jwtService: JwtService
) {
    @Bean
    fun jwtFilter(): JwtTokenAuthenticationFilter {
        return JwtTokenAuthenticationFilter(jwtService)
    }
}