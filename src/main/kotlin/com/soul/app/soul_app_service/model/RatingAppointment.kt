package com.soul.app.soul_app_service.model

data class RatingAppointment(
    val id : Int,
    val appointmentId : Int,
    val psychologyId : Int,
    val userId : Int,
    val rate : Int,
    val description : String
)
