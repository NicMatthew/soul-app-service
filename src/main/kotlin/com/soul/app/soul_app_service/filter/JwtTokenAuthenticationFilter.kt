package com.soul.app.soul_app_service.filter

import com.soul.app.soul_app_service.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtTokenAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                if (jwtService.verifyToken(token)) {
                    val userId = jwtService.getUserIdFromToken(token)
                        ?: throw RuntimeException("Invalid JWT")

                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        emptyList()
                    )

                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
