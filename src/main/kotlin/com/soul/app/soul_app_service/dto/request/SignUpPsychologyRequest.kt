package com.soul.app.soul_app_service.dto.request

import java.sql.Date

data class SignUpPsychologyRequest(
    val name: String,
    val email: String,
    val username: String,
    val password_hash: String,
    val phone: String,
    val dob: Date,
    val gender: String,


    val alumnus : String,
    val sipp: String,
    val careerStartDate: Date,
    val pricePerSession: Int,
    val education: String,
    val clinic: String,
    val description :String,
    val field_id : List<Int>
)
