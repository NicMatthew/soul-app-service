package com.soul.app.soul_app_service.model

import java.sql.Date

data class PsychologyProfile(
    val id: Int,
    val userId: Int,
    val alumnus : String,
    val sipp: String,
    val careerStartDate: Date,
    val pricePerSession: Int,
    val religion: String,
    val education: String,
    val clinic: String,
    val description :String,
    val rating : Float? = 0F
)
