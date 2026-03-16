package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.response.GetPsychologyDetailResponse
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.service.AppointmentService
import com.soul.app.soul_app_service.service.PsychologyService
import com.soul.app.soul_app_service.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/public")
@Tag(
    name = "Public Controller ( No Token Needed )",
)
class PublicController(
    val psychologyService: PsychologyService,
    private val appointmentService: AppointmentService
) {
    @GetMapping("/psychology/{psychologyId}")
    @Operation(
        summary = "Get Psycholog Detail",
    )
    fun getPsychologyDetail(
        @PathVariable("psychologyId") psychologyId: Int
    ): ResponseEntity<GetPsychologyDetailResponse>
    {
        return ResponseEntity.ok(GetPsychologyDetailResponse(
            psychology = psychologyService.getPsychologyDetailByUserId(psychologyId)?:throw Exception("INVALID PSYCHOLOG ID"),
            slots = appointmentService.getWeeklyAvailabilityWithStatus(psychologyId)
        ))
    }
    @GetMapping("/psychology")
    @Operation(
        summary = "Get All Psycholog",
    )
    private fun getAllPyschology(
        @RequestParam(name = "search") search: String?
    ): ResponseEntity<List<Psychology>> {
        return ResponseEntity.ok(psychologyService.getAllPsychologies(search))
    }
}