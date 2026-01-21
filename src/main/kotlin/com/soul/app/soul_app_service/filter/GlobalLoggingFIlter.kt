package com.soul.app.soul_app_service.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

@Component
class GlobalLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(GlobalLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        val start = System.currentTimeMillis()

        filterChain.doFilter(wrappedRequest, wrappedResponse)

        val duration = System.currentTimeMillis() - start

        logRequest(wrappedRequest)
        logResponse(wrappedResponse, duration)

        wrappedResponse.copyBodyToResponse()
    }

    private fun logRequest(request: ContentCachingRequestWrapper) {
        val body = String(
            request.contentAsByteArray,
            StandardCharsets.UTF_8
        )

        log.info(
            """
            ▶ REQUEST
            Method : {}
            URI    : {}
            Headers: {}
            Body   : {}
            """.trimIndent(),
            request.method,
            request.requestURI,
            request.headerNames.toList().associateWith { request.getHeader(it) },
            body.ifBlank { "-" }
        )
    }

    private fun logResponse(
        response: ContentCachingResponseWrapper,
        duration: Long
    ) {
        val body = String(
            response.contentAsByteArray,
            StandardCharsets.UTF_8
        )

        log.info(
            """
            ◀ RESPONSE
            Status  : {}
            Time    : {} ms
            Headers : {}
            Body    : {}
            """.trimIndent(),
            response.status,
            duration,
            response.headerNames.associateWith { response.getHeader(it) },
            body.ifBlank { "-" }
        )
    }
}