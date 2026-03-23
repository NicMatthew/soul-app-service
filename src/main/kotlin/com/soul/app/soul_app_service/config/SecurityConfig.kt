package com.soul.app.soul_app_service.config

import com.soul.app.soul_app_service.filter.GlobalLoggingFilter
import com.soul.app.soul_app_service.filter.JwtTokenAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtTokenAuthenticationFilter,
    private val globalLoggingFilter: GlobalLoggingFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/auth/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/public/**",
                    "/payment/**",
                    "/ws/**"
                ).permitAll()

                it.requestMatchers("/psychology/**")
                    .hasRole("psycholog")

                it.requestMatchers("/admin/**")
                    .hasRole("admin")

                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, ex ->
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = "application/json"
                    response.writer.write(
                        """{"error":"Unauthorized","message":"${ex.message}"}"""
                    )
                }

                it.accessDeniedHandler { _, response, ex ->
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.contentType = "application/json"
                    response.writer.write(
                        """{"error":"Forbidden","message":"${ex.message}"}"""
                    )
                }
            }
            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterBefore(
                globalLoggingFilter,
                JwtTokenAuthenticationFilter::class.java
            )

        return http.build()
    }
}