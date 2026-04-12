package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.request.AddPsychologyCertificateRequest
import com.soul.app.soul_app_service.dto.request.DeletePsychologyCertificateRequest
import com.soul.app.soul_app_service.dto.request.SignUpPsychologyRequest
import com.soul.app.soul_app_service.dto.response.GetAllPsychologyResponse
import com.soul.app.soul_app_service.model.Field
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyCertificate
import com.soul.app.soul_app_service.service.AdminService
import com.soul.app.soul_app_service.service.PsychologyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
@Tag(
    name = "Admin Controller",
)
class AdminController(
    private val adminService: AdminService,
    private val psychologyService: PsychologyService
) {
    private val uploadDir = "/var/www/assets/certificates/"

    @PostMapping("/add-psychology")
    @Operation(
        summary = "Admin add psycholog account",
    )
    private fun signUpPyschology(
        @RequestBody signUpPsychologyRequest: SignUpPsychologyRequest
    ): ResponseEntity<Psychology> {
        return ResponseEntity.ok(adminService.signUpPyschology(signUpPsychologyRequest))
    }

    @GetMapping("/fields")
    @Operation(
        summary = "Get fields for add psycholog account",
    )
    private fun getAllFields(): ResponseEntity<List<Field>> {
        return ResponseEntity.ok(psychologyService.getAllFields())
    }
    @GetMapping("/psychologies")
    @Operation(
        summary = "Get All Pychologies",
    )
    private fun getAllPyschology(
        @RequestParam(name = "search") search: String?
    ): ResponseEntity<List<GetAllPsychologyResponse>> {
        return ResponseEntity.ok(psychologyService.getAllPsychologies(search,null,null,null))
    }
    @DeleteMapping("/delete-psychology")
    @Operation(
        summary = "Delete psychology Account",
    )
    private fun deletePsychology(
        @RequestBody userId: Int
    ):ResponseEntity<String> {
        return ResponseEntity.ok(adminService.deletePsychologyAccount(userId))
    }
    @PostMapping("/add-certificate")
    @Operation(
        summary = "Add certificate for psychology account",
    )
    private fun addCertificate(
        @RequestBody certificate : AddPsychologyCertificateRequest
    ): ResponseEntity<PsychologyCertificate> {
        return ResponseEntity.ok(adminService.addCertificate(certificate))
    }

    @DeleteMapping("/delete-certificate")
    @Operation(
        summary = "Delete certificate for psychology account",
    )
    private fun deleteCertificate(
        @RequestBody certificate : DeletePsychologyCertificateRequest
    ): ResponseEntity<String> {
        return ResponseEntity.ok(adminService.deleteCertificate(certificate))
    }
}