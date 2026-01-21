package com.soul.app.soul_app_service.filter

import com.soul.app.soul_app_service.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtTokenAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        return path.startsWith("/auth")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
    }


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                val decoded = jwtService.decode(token)

                val userId = decoded.subject.toInt()
                val role = decoded.getClaim("role").asString()

                val authentication = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_$role"))
                )

                SecurityContextHolder.getContext().authentication = authentication
            } catch (ex: Exception) {
                SecurityContextHolder.clearContext()
                throw RuntimeException("JWT token is invalid or expired, ${ex.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}
