package com.soul.app.soul_app_service.dto.response

import com.soul.app.soul_app_service.model.Appointment

data class GetPsychologAppointmentResponse(
    val appointment: Appointment,
    val anonymous: Boolean,
    val clientName: String
)
