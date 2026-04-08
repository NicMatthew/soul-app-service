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
            response.add(
                RatingAppResponse(
                    rate = it.rate,
                    description = it.description,
                    userName = userRepository.getUserById(it.userId)?.name!!,
                    userProfile = userRepository.getUserById(it.userId)?.profile_picture!!,
                )
            )
        }
        return response
    }
}