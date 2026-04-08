package com.soul.app.soul_app_service.dto

data class RecommendationResult(
    val psychologistId: Int,
    val score: Double,
    val reasons: String?
)
