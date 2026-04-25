package com.soul.app.soul_app_service.model

import java.sql.Date

data class Appointment(
    val id: Int,
    val clientUserId : Int,
    val psychologyId : Int,
    val scheduledAt : String,
    val date : Date,
    val startTime : String,
    val endTime : String,
    val status : String,
    val finalDiagnose: String? = null,
    val medicalNotes : String? = null,
)
