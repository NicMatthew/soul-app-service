package com.soul.app.soul_app_service.dto.response

import java.sql.Date

data class GetAllPsychologyResponse (
    val profileId : Int,
    val userId: Int,
    val name:String,
    val profilePicture: String,
    val rating: Float,
    val careerStartDate: Date,
    val description: String,
    val pricePerSession: Int
)