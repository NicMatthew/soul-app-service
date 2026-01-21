package com.soul.app.soul_app_service.model

import java.sql.Date
import java.sql.Time

data class AppointmentSlot(
    val id: Int,
    val psychologyId: Int,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val status: String,
    val appointmentId: Int? = null
)

