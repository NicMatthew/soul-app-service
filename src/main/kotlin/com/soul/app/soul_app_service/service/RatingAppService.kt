package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.response.RatingAppResponse
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class RatingAppService(private val userRepository: UserRepository) {
    fun getAllRatingApp(): List<RatingAppResponse>? {
        val rating = userRepository.getAllRatingApp()
        val response = mutableListOf<RatingAppResponse>()
        rating.forEach {
            val user = userRepository.getUserById(it.userId)!!
            response.add(
                RatingAppResponse(
                    rate = it.rate,
                    description = it.description,
                    userName = if(!user.anonymous) user.name else "Anonymous",
                    userProfile = if(!user.anonymous) user.profile_picture else null,
                )
            )
        }
        return response
    }
}