package com.soul.app.soul_app_service.model

data class PreferenceQuestionOption(
    val id: Int,
    val questionId: Int,
    val code: String,
    val label: String,
    val weight: Int
)