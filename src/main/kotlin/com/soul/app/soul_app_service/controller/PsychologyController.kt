package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.CreatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyAvailability
import com.soul.app.soul_app_service.service.PsychologyService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/psychology")
class PsychologyController(
    private val psychologyService: PsychologyService
) {

    @GetMapping("/availability")
    fun getPsychologyAvailability(
    ): ResponseEntity<List<PsychologyAvailability>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(psychologyService.getPsychologyAvailibility(userId))
    }

    @PostMapping("/create-availability")
    fun createPsychologyAvailability(
        @RequestBody availability: List<CreatePsychologyAvailabilityRequest>
    ): ResponseEntity<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        psychologyService.createPsychologyAvailibility(availability,userId)
        return ResponseEntity.ok("created")
    }
    @GetMapping("/psychologies")
    fun getAllPyschology(
    ): ResponseEntity<List<Psychology>> {
        return ResponseEntity.ok(psychologyService.getAllPsychologies())
    }

    @GetMapping("/me")
    fun me(
    ): ResponseEntity<Psychology> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.principal as Int
        return ResponseEntity.ok(psychologyService.getPsychologyDetailByUserId(userId))
    }
//    @GetMapping("/detail")
//    fun getPsychologySlots(
//    ): ResponseEntity<Psychology> {
//        val authentication = SecurityContextHolder.getContext().authentication
//        val userId = authentication.principal as Int
//        return ResponseEntity.ok(psychologyService.getPsychologyDetail(userId))
//    }






}