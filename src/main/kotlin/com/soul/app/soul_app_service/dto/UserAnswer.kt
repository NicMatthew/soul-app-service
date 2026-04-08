package com.soul.app.soul_app_service.dto

data class UserAnswer(
    val questionId: Int,
    val optionId: Int,
    val questionCode: String,
    val questionWeight: Double,
    val optionCode: String,
    val optionWeight: Double
)
