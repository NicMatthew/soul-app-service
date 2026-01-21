package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.LoginRequest
import com.soul.app.soul_app_service.dto.request.LoginResponse
import com.soul.app.soul_app_service.dto.request.SignUpRequest
import com.soul.app.soul_app_service.service.AuthService
import com.soul.app.soul_app_service.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService
) {

        @PostMapping("/login")
        fun login(
            @RequestBody request: LoginRequest,
        ): ResponseEntity<LoginResponse> {

            val token = authService.login(request)
            val user = userService.getUserByEmail(request.email)

            return ResponseEntity.ok(LoginResponse(user, token))
    }
    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<String> {

        val cookie = Cookie("ACCESS_TOKEN", "")
        cookie.isHttpOnly = true
        cookie.path = "/"
        cookie.maxAge = 0   // 🔥 hapus cookie

        response.addCookie(cookie)

        return ResponseEntity.ok("Logout successful")
    }

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<String> {
        return ResponseEntity.ok(authService.signUp(request))
    }
}