package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.response.PreferenceQuestionResponse
import com.soul.app.soul_app_service.service.PreferenceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
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

    @GetMapping("/questions")
    @Operation(
        summary = "Get Preference Questions",
    )
    fun getPreferenceQuestions():
            ResponseEntity<List<PreferenceQuestionResponse>> {

        return ResponseEntity.ok(
            preferenceService.getAllQuestions()
        )
    }
    @PostMapping("/submit")
    @Operation(
        summary = "Get Preference Questions",
    )
    fun submitPreference(
        @RequestBody preferenceQuestions: List<Int>
    ): ResponseEntity<List<PreferenceQuestionResponse>> {

        return ResponseEntity.ok(
            preferenceService.getAllQuestions()
        )
    }
}