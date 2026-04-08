package com.soul.app.soul_app_service.dto.response

import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.Payment

data class GetUserAppointmentDetailResponse(
    val appointment: Appointment,
    val payment : Payment,
    val psychologName : String,
    val rating : RatingResponse? = null
)
