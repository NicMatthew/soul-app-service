package com.soul.app.soul_app_service.model

data class Psychology(
    val user: User,
    val psychologyProfile: PsychologyProfile,
    val fields :List<Field>,
    val certificates: List<PsychologyCertificate>?,
    val ratings: List<RatingAppointment>?
    )
