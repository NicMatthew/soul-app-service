package com.soul.app.soul_app_service.dto.request

import com.soul.app.soul_app_service.model.Field
import java.sql.Date

data class UpdateProfilePsychologyRequest (
    val name: String,
    val email: String,
    val password_hash: String,
    val username: String,
    val phone: String,
    val dob: Date,
    val gender: String,
    val profile_picture: String? = null,
    val alumnus : String,
    val sipp: String,
    val careerStartDate: Date,
    val pricePerSession: Int,
    val education: String,
    val clinic: String,
    val description :String,
    val religion :String,
    val fields :List<Field>
)