package com.soul.app.soul_app_service.dto.request

import com.soul.app.soul_app_service.model.User

data class LoginResponse(
    val user : User,
    val token : String
)
