package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.CreateAppointmentRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfileRequest
import com.soul.app.soul_app_service.dto.response.GetPsychologyDetailResponse
import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.service.PsychologyService
import com.soul.app.soul_app_service.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController (
    private val userService: UserService,
){
    @PutMapping("/edit-profile")
    fun updateProfile(
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<String> {
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



    @PostMapping("/create-appointment")
    fun createAppointment(
        @RequestBody appointment: CreateAppointmentRequest
    ):ResponseEntity<Appointment> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int

        return ResponseEntity.ok(userService.createAppointment(userId, appointment))
    }
}