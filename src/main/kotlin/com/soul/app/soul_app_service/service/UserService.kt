package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.UpdateProfileRequest
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun updateProfile(
        userId: Int,
        request: UpdateProfileRequest
    ): String {

        val user = userRepository.getUserById(userId)
            ?: throw IllegalArgumentException("User not found")

        val updatedUser = user.copy(
            username = request.username ?: user.username,
            phone = request.phone ?: user.phone,
            dob = request.dob ?: user.dob,
            gender = request.gender ?: user.gender,
            profile_picture = request.profilePicture ?: user.profile_picture,
            anonymous = request.anonymous ?: user.anonymous
        )

        userRepository.updateUser(updatedUser)

        return "user updated"
    }
    fun getUserByEmail(email: String): User {
        return (userRepository.getUserByEmail(email)?: userRepository.getUserByUsername(email))!!
    }
    fun getUserById(userId: Int): User? {
        return userRepository.getUserById(userId)
    }
}
