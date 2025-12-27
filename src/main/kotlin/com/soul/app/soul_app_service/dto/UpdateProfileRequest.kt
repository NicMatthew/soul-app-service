package com.soul.app.soul_app_service.dto


import java.sql.Date

data class UpdateProfileRequest(
    val username: String?,
    val phone: String?,
    val dob: Date?,
    val gender: String?,
    val profilePicture: String?,
    val anonymous: Boolean?
)

