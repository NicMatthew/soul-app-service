package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.LoginRequest
import com.soul.app.soul_app_service.dto.request.LoginResponse
import com.soul.app.soul_app_service.dto.request.SignUpRequest
import com.soul.app.soul_app_service.service.AuthService
import com.soul.app.soul_app_service.service.PaymentService
import com.soul.app.soul_app_service.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment")
@Tag(
    name = "MIDTRANS CALLBACK ( DO NOT USE )",
)
    class PaymentController(
    private val paymentService: PaymentService
) {
    @PostMapping("/notification")
    fun handleNotification(
        @RequestBody payload: Map<String, Any>
    ): ResponseEntity<String> {

        paymentService.handleMidtransNotification(payload)

        return ResponseEntity.ok("OK")
    }
}