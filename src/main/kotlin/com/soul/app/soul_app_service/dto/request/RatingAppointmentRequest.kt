package com.soul.app.soul_app_service.dto.request

import org.springframework.context.annotation.Description

data class RatingAppointmentRequest (
    val rate : Int,
    val description: String,
)