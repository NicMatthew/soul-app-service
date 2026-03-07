package com.soul.app.soul_app_service.dto.response

import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.Payment
import com.soul.app.soul_app_service.model.Psychology

data class GetAppointmentDetailResponse(
    val appointment: Appointment,
    val payment : Payment,
)
