package com.soul.app.soul_app_service.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception): ResponseEntity<Any> {

        val status = when (ex) {
            is IllegalArgumentException -> 400
            is IllegalStateException -> 400
            else -> 500
        }

        return ResponseEntity
            .status(status)
            .body(
                mapOf(
                    "error" to (if (status == 500) "Internal Server Error" else "Bad Request"),
                    "message" to (ex.message ?: ex.localizedMessage ?: ex.stackTraceToString())
                )
            )
    }
}