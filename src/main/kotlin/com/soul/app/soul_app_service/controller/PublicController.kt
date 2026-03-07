package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.response.GetPsychologyDetailResponse
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.service.PsychologyService
import com.soul.app.soul_app_service.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/public")
class PublicController(
    val psychologyService: PsychologyService,
    val userService: UserService
) {
    @GetMapping("/psychology/{psychologyId}")
    fun getPsychologyDetail(
        @PathVariable("psychologyId") psychologyId: Int
    ): ResponseEntity<GetPsychologyDetailResponse>
    {
        return ResponseEntity.ok(GetPsychologyDetailResponse(
            psychology = psychologyService.getPsychologyDetailByUserId(psychologyId)?:throw Exception("INVALID PSYCHOLOG ID"),
            slots = userService.getWeeklyAvailabilityWithStatus(psychologyId)
        ))
    }
    @GetMapping("/psychology")
    private fun getAllPyschology(
    ): ResponseEntity<List<Psychology>> {
        return ResponseEntity.ok(psychologyService.getAllPsychologies())
    }
}