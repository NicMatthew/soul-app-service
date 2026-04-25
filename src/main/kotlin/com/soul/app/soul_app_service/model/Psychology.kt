package com.soul.app.soul_app_service.model

import com.soul.app.soul_app_service.dto.response.RatingResponse

data class Psychology(
    val user: User,
    val psychologyProfile: PsychologyProfile,
    val fields :List<Field>,
    val certificates: List<PsychologyCertificate>?,
    val ratings: List<RatingResponse>?
    )
