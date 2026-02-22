package com.soul.app.soul_app_service.dto.request

import java.sql.Date

data class CreateAppointmentRequest(
    var psychologyId : Int,
    val startTime : String,
    val endTime : String,
    val date: Date
)
