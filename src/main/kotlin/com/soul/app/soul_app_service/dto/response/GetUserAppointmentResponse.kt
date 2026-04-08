package com.soul.app.soul_app_service.dto.response

import com.soul.app.soul_app_service.model.Appointment

data class GetUserAppointmentResponse(
    val appointment: Appointment,
    val psychologName : String,
    val rating : Int? = null
)
