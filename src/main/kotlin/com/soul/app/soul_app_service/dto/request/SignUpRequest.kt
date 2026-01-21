package com.soul.app.soul_app_service.dto.request

import java.sql.Date

data class SignUpRequest(
    val name: String,
    val email: String,
    val username: String,
    val password_hash: String,
    val phone: String,
    val dob: Date,
    val gender: String,
)
