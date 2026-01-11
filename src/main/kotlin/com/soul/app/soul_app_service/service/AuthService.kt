package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.LoginRequest
import com.soul.app.soul_app_service.dto.SignUpRequest
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    ) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun login(request: LoginRequest): String {
        try {
            val user = userRepository.getUserByEmail(request.email) ?: userRepository.getUserByUsername(request.email)
            if (user != null && user.password_hash == request.password) {
                return jwtService.generateToken(user.id)
            } else {
                throw RuntimeException("Invalid email or password")
            }
        } catch (e: Exception) {
            logger.error("Error during login process", e)
            throw RuntimeException("Internal Server Error")
        }
    }

    fun signUp(request: SignUpRequest): String {
        try {
            val user = User(
                id = -99,
                name = request.name,
                email = request.email,
                password_hash = request.password_hash,
                username = request.username,
                phone = request.phone,
                role = "user",
                dob = request.dob,
                gender = request.gender
            )
            userRepository.saveUser(user)
            return "Success"
        } catch (e: Exception) {
            logger.error("Error during sign up process", e)
            throw RuntimeException("Internal Server Error")
        }
    }
}