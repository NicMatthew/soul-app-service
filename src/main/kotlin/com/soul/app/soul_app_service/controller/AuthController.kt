package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.LoginRequest
import com.soul.app.soul_app_service.dto.SignUpRequest
import com.soul.app.soul_app_service.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): String {
        println(authService.login(request))
        return authService.login(request)
    }

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpRequest): String {
        return authService.signUp(request)
    }
}