package com.soul.app.soul_app_service.dto.response

import java.sql.Date

data class GetAllPsychologyResponse (
    val id : Int,
    val userId: Int,
    val profilePicture: String,
    val rating: Float,
    val careerStartDate: Date,
    val description: String,
    val pricePerSession: Int
)