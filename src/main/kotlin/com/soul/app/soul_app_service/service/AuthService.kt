package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.request.LoginRequest
import com.soul.app.soul_app_service.dto.request.SignUpRequest
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    ) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun login(request: LoginRequest): String {
        val user = userRepository.getUserByEmail(request.email) ?: userRepository.getUserByUsername(request.email)
        if (user != null && user.password_hash == request.password) {
            return jwtService.generateToken(user.id,user.role)
        } else {
            throw RuntimeException("Invalid email or password")
        }

    }

    fun signUp(request: SignUpRequest): User {
        val user = User(
            id = -99,
            name = request.name,
            email = request.email,
            password_hash = request.password_hash,
            username = request.username,
            phone = request.phone,
            role = "user",
            dob = request.dob,
            gender = request.gender,
            anonymous = false,
        )
        val userId = userRepository.saveUser(user)
        return userRepository.getUserById(userId)!!

    }
}