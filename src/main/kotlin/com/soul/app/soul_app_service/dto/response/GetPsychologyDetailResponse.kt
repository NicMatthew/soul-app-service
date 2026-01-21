package com.soul.app.soul_app_service.dto.response

import com.soul.app.soul_app_service.dto.TimeSlotWithStatus
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyAvailability
import com.soul.app.soul_app_service.model.PsychologyProfile
import java.time.LocalDate

data class GetPsychologyDetailResponse(
    val psychology: Psychology,
    val slots : Map<LocalDate, List<TimeSlotWithStatus>>
)