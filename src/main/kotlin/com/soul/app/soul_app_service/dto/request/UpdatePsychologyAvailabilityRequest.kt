package com.soul.app.soul_app_service.dto.request

data class UpdatePsychologyAvailabilityRequest(
    val id: Int,
    val dayOfWeek : Int,
    val startTime: String,
    val endTime: String,
    val flag: String,
)
