package com.soul.app.soul_app_service.dto.request

data class CreatePsychologyAvailabilityRequest (
    val dayOfWeek : Int,
    val startTime: String,
    val endTime: String,
)