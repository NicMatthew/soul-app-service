package com.soul.app.soul_app_service.dto.response

data class RatingAppResponse(
    val userName: String,
    val userProfile: String?,
    val rate: Int,
    val description: String,
)
