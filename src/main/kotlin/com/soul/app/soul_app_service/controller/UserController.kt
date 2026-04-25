package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.CreateAppointmentRequest
import com.soul.app.soul_app_service.dto.request.CreatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.dto.request.RatingAppRequest
import com.soul.app.soul_app_service.dto.request.RatingAppointmentRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfileRequest
import com.soul.app.soul_app_service.dto.response.GetUserAppointmentDetailResponse
import com.soul.app.soul_app_service.dto.response.GetUserAppointmentResponse
import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.Notification
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.service.AppointmentService
import com.soul.app.soul_app_service.service.UserService
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
@RequestMapping("/user")
@Tag(
    name = "User Controller",
)

class UserController (
    private val userService: UserService,
    private val appointmentService: AppointmentService,
){
    @PutMapping("/edit-profile")
    @Operation(
        summary = "Edit User Profile",
    )
    fun updateProfile(
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<User> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(userService.updateProfile(userId, request))
    }

    @GetMapping("/me")
    fun me(
    ): ResponseEntity<User> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(userService.getUserById(userId))
    }


    @GetMapping("/appointment")
    @Operation(
        summary = "Get User's All Appointment",
    )
    fun getAllAppointments(
        @RequestParam(value = "status", required = false) status: String?,
        @RequestParam(value = "date", required = false) date: Date?,
        @RequestParam(value = "order", required = false) order: String?,
    ): ResponseEntity<List<GetUserAppointmentResponse>?> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(appointmentService.getAllUserAppointments(userId,status,date,order))
    }
    @GetMapping("/appointment/{appointment-id}")
    @Operation(
        summary = "Get User's Appointment Detail",
    )
    fun getAppointmentDetail(
        @PathVariable("appointment-id") appointmentId: String,
    ): ResponseEntity<GetUserAppointmentDetailResponse> {
        val id = appointmentId.toInt()
        return ResponseEntity.ok(appointmentService.getAppointmentDetail(id))
    }

    @PostMapping("/create-appointment")
    @Operation(
        summary = "Create Appointment",
    )
    fun createAppointment(
        @RequestBody appointment: CreateAppointmentRequest
    ):ResponseEntity<Appointment> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(appointmentService.createAppointment(userId, appointment))
    }

    @PostMapping("/rate-appointment/{appointment-id}")
    @Operation(
        summary = "Rating Appointment",
    )
    fun rateAppointment(
        @RequestBody request: RatingAppointmentRequest, @PathVariable("appointment-id") appointmentId: Int
    ):ResponseEntity<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(appointmentService.rateAppointment(userId,request,appointmentId))
    }
    @PostMapping("rate-app")
    @Operation(
        summary = "Submit Rating App",
    )
    fun submitRatingApp(
        @RequestBody request: RatingAppRequest
    ): ResponseEntity<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(userService.submitRatingApp(userId,request))
    }

    @GetMapping("rate-eligible")
    @Operation(
        summary = "Check user eligiblity for Rating App",
    )
    fun checkEligibilityRatingApp(
    ): ResponseEntity<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(userService.checkUserEligibility(userId))
    }

    @GetMapping("notification")
    @Operation(
        summary = "Get User's Notification",
    )
    fun getNotifications(): ResponseEntity<List<Notification>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(userService.getUserNotifications(userId))
    }


}