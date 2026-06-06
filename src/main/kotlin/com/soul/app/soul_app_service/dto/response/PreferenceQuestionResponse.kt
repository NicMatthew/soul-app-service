package com.soul.app.soul_app_service.dto.response

data class PreferenceQuestionResponse(
    val id: Int,
    val question: String,
    val multipleAnswer : Boolean,
    val required : Boolean,
    val options: List<PreferenceOptionResponse>
)