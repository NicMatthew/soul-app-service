package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.LoginRequest
import com.soul.app.soul_app_service.dto.SignUpRequest
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.service.AuthService
import com.soul.app.soul_app_service.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.CacheControl.maxAge
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
        response: HttpServletResponse
    ): ResponseEntity<User> {

        val token = authService.login(request)

        val cookie = Cookie("ACCESS_TOKEN", token).apply {
            isHttpOnly = true
            secure = false
            path = "/"
            maxAge = 60 * 60 * 24 // 1 hari
        }

        response.addCookie(cookie)
        val user = userService.getUserByEmail(request.email)

        return ResponseEntity.ok(user)
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
    fun signUp(@RequestBody request: SignUpRequest): String {
        return authService.signUp(request)
    }
}