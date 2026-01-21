package com.soul.app.soul_app_service.model

data class Appointment(
    val id: Int,
    val clientUserId : Int,
    val psychologyId : Int,
    val scheduledAt : String,
    val startTime : String,
    val endTime : String,
    val status : String,
    val notesPsychology: String? = null,
    val description : String? = null,
)
