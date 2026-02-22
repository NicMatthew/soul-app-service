package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.SignUpPsychologyRequest
import com.soul.app.soul_app_service.model.Field
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.service.AdminService
import com.soul.app.soul_app_service.service.PsychologyService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService,
    private val psychologyService: PsychologyService
) {

    @PostMapping("/sign-up-psychology")
    private fun signUpPyschology(
        @RequestBody signUpPsychologyRequest: SignUpPsychologyRequest
    ): ResponseEntity<Psychology> {
        return ResponseEntity.ok(adminService.signUpPyschology(signUpPsychologyRequest))
    }

    @GetMapping("/fields")
    private fun getAllFields(): ResponseEntity<List<Field>> {
        return ResponseEntity.ok(adminService.getAllFields())
    }
    @GetMapping("/psychologies")
    private fun getAllPyschology(
    ): ResponseEntity<List<Psychology>> {
        return ResponseEntity.ok(psychologyService.getAllPsychologies())
    }
}