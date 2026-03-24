package com.soul.app.soul_app_service.dto.request

import java.sql.Date

data class SignUpPsychologyRequest(
    val name: String,
    val email: String,
    val username: String,
    val phone: String,
    val dob: Date,
    val gender: String,
    val religion: String,


    val alumnus : String,
    val sipp: String,
    val careerStartDate: Date,
    val pricePerSession: Int,
    val education: String,
    val clinic: String,
    val description :String,
    val fieldId : List<Int>
)
