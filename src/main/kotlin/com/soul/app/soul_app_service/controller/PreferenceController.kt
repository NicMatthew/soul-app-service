package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.RecommendationResult
import com.soul.app.soul_app_service.dto.request.PreferenceAnswerRequest
import com.soul.app.soul_app_service.dto.response.PreferenceQuestionResponse
import com.soul.app.soul_app_service.dto.response.RecommendationResultResponse
import com.soul.app.soul_app_service.service.PreferenceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/preferences")
@Tag(
    name = "Preference Controller",
)
class PreferenceController(
    val preferenceService: PreferenceService
) {

    @GetMapping("/questions/user")
    @Operation(
        summary = "Get Preference Questions",
    )
    fun getUserPreferenceQuestions():
            ResponseEntity<List<PreferenceQuestionResponse>> {

        return ResponseEntity.ok(
            preferenceService.getAllUserQuestions()
        )
    }
    @PostMapping("/submit/user")
    @Operation(
        summary = "Get Preference Questions",
    )
    fun submitUserPreference(
        @RequestBody preferenceQuestions: List<PreferenceAnswerRequest>
    ): ResponseEntity<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(
            preferenceService.submitPreferenceUser(userId,preferenceQuestions)
        )
    }

    @GetMapping("/questions/psycholog")
    @Operation(
        summary = "Get Preference Questions",
    )
    fun getPsychologPreferenceQuestions():
            ResponseEntity<List<PreferenceQuestionResponse>> {

        return ResponseEntity.ok(
            preferenceService.getAllPsychologistQuestions()
        )
    }
    @PostMapping("/submit/psycholog")
    @Operation(
        summary = "Get Preference Questions",
    )
    fun submitPsychologPreference(
        @RequestBody preferenceQuestions: List<PreferenceAnswerRequest>
    ): ResponseEntity<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(
            preferenceService.submitPreferencePsycholog(userId,preferenceQuestions)
        )
    }
    @GetMapping("/recommendation")
    @Operation(summary = "Get psychologist recommendations for user")
    fun getRecommendations(): ResponseEntity<List<RecommendationResultResponse>> {
        val userId = SecurityContextHolder.getContext().authentication.principal as Int
        return ResponseEntity.ok(preferenceService.getRecommendations(userId))
    }
}