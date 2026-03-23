package com.soul.app.soul_app_service.model

import java.sql.Date


data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password_hash: String,
    val username: String,
    val phone: String,
    val role: String,
    val dob: Date,
    val gender: String,
    val profile_picture: String? = null,
    val anonymous: Boolean,
)