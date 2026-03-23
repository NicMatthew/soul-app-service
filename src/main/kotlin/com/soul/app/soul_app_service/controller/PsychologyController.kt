package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.CreatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfilePsychologyRequest
import com.soul.app.soul_app_service.dto.request.addAppointmentNotesRequest
import com.soul.app.soul_app_service.dto.response.GetPsychologAppointmentResponse
import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyAvailability
import com.soul.app.soul_app_service.service.AppointmentService
import com.soul.app.soul_app_service.service.PsychologyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Date

@RestController
@RequestMapping("/psychology")
@Tag(
    name = "Psycholog Controller",
)
class PsychologyController(
    private val psychologyService: PsychologyService,
    private val appointmentService: AppointmentService
) {

    @PutMapping("/edit-profile")
    @Operation(
        summary = "Edit Psycholog Profile",
    )
    fun updateProfile(
        @RequestBody request: UpdateProfilePsychologyRequest
    ): ResponseEntity<Psychology> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(psychologyService.updateProfile(userId, request))
    }


    @GetMapping("/availability")
    @Operation(
        summary = "Get Psycholog Schedule",
    )
    fun getPsychologyAvailability(
    ): ResponseEntity<List<PsychologyAvailability>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(psychologyService.getPsychologyAvailibility(userId))
    }

    @PostMapping("/create-availability")
    @Operation(
        summary = "Create Schedule",
    )
    fun createPsychologyAvailability(
        @RequestBody availability: List<CreatePsychologyAvailabilityRequest>
    ): ResponseEntity<List<PsychologyAvailability>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(psychologyService.createPsychologyAvailibility(availability,userId))
    }

    @PostMapping("/create-notes/{appointmentId}")
    @Operation(
        summary = "Add Appointment Notes (when appointment finished)",
    )
    fun addAppointmentNotes(
        @RequestBody request: addAppointmentNotesRequest, @PathVariable appointmentId: String
    ): ResponseEntity<Appointment> {

        return ResponseEntity.ok(appointmentService.addAppointmentNotes(request,appointmentId.toInt()))
    }

    @GetMapping("/appointment")
    @Operation(
        summary = "Get Psychology's All Appointment",
    )
    fun getAllAppointments(
        @RequestParam(value = "status", required = false) status: String?,
        @RequestParam(value = "date", required = false) date: Date?,
        @RequestParam(value = "order", required = false) order: String?,
    ): ResponseEntity<List<GetPsychologAppointmentResponse>?> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(appointmentService.getAllPsychologAppointments(userId,status,date,order))
    }

    @GetMapping("/appointment/{clientUserId}")
    @Operation(
        summary = "Get Patient's All Appointment history",
    )
    fun getPatientsHistory(
        @PathVariable clientUserId: String
    ): ResponseEntity<List<GetPsychologAppointmentResponse>?> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(appointmentService.getPatientsHistory(clientUserId.toInt(),userId))
    }

    @GetMapping("/me")
    fun me(
    ): ResponseEntity<Psychology> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(psychologyService.getPsychologyDetailByUserId(userId))
    }








}