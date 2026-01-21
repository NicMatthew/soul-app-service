package com.soul.app.soul_app_service.model


data class PsychologyAvailability (
    val id : Int?,
    val psychologistId : Int?,
    val dayOfWeek : Int,
    val startTime: String,
    val endTime: String,
    val status : String? = null,
)