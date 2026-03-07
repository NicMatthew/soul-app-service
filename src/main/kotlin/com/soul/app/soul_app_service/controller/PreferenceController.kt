package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.response.PreferenceQuestionResponse
import com.soul.app.soul_app_service.service.PreferenceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/public/preferences")
class PreferenceController(
    val preferenceService: PreferenceService
) {

    @GetMapping("/questions")
    fun getPreferenceQuestions():
            ResponseEntity<List<PreferenceQuestionResponse>> {

        return ResponseEntity.ok(
            preferenceService.getAllQuestions()
        )
    }
}