package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.SignUpPsychologyRequest
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(val adminService: AdminService) {
    @GetMapping("/psychologies")
    fun getAllPyschology(
    ): ResponseEntity<List<Psychology>> {
        return ResponseEntity.ok(adminService.getAllPsychologies())
    }
    @PostMapping("/sign-up-psychology")
    fun signUpPyschology(
        @RequestBody signUpPsychologyRequest: SignUpPsychologyRequest
    ): ResponseEntity<Psychology> {
        return ResponseEntity.ok(adminService.signUpPyschology(signUpPsychologyRequest))
    }
}