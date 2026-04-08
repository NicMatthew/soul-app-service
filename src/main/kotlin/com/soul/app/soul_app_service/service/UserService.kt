package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.request.RatingAppRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfileRequest
import com.soul.app.soul_app_service.dto.response.GetUserAppointmentDetailResponse
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.repository.AppointmentRepository
import com.soul.app.soul_app_service.repository.PaymentRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val paymentRepository: PaymentRepository,
    private val psychologyRepository: PsychologyRepository,

    ) {

    fun updateProfile(
        userId: Int,
        request: UpdateProfileRequest
    ): User {

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



        return userRepository.getUserById(userRepository.updateUser(updatedUser))!!
    }
    fun getUserByEmail(email: String): User {
        return (userRepository.getUserByEmail(email)?: userRepository.getUserByUsername(email))!!
    }
    fun getUserById(userId: Int): User? {
        return userRepository.getUserById(userId)
    }
    fun submitRatingApp(
        userId: Int,
        request : RatingAppRequest
    ): String{
        userRepository.submitRatingApp(userId, request)
        return "OK"
    }







}
