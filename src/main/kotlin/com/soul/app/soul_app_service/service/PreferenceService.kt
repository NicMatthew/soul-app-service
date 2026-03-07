package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.response.PreferenceOptionResponse
import com.soul.app.soul_app_service.dto.response.PreferenceQuestionResponse
import com.soul.app.soul_app_service.repository.PreferenceRepository
import org.springframework.stereotype.Service

@Service
class PreferenceService(
    private val preferenceRepository: PreferenceRepository
) {

    fun getAllQuestions(): List<PreferenceQuestionResponse> {

        val questions = preferenceRepository.getAllQuestions()

        return questions.map { question ->

            val options = preferenceRepository
                .getOptionsByQuestionId(question.id)
                .map {
                    PreferenceOptionResponse(
                        id = it.id,
                        label = it.label
                    )
                }

            PreferenceQuestionResponse(
                id = question.id,
                question = question.description,
                options = options
            )
        }
    }
}