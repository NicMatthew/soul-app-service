package com.soul.app.soul_app_service.controller

import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema

@RestController
@RequestMapping("/upload")
class FileUploadController {

    private val uploadDir = "/var/www/assets"

    @PostMapping("/certificate",consumes = ["multipart/form-data"])
    fun uploadCertificate(
        @Parameter(
            description = "File to upload",
            required = true,
            content = [Content(mediaType = "multipart/form-data")]
        )
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {

        if (file.isEmpty) {
            throw RuntimeException("File is empty")
        }
        val fileName = System.currentTimeMillis().toString() + "_" + file.originalFilename

        val dest = File("$uploadDir/certificates/$fileName")

        // pastikan folder ada
        dest.parentFile.mkdirs()

        // save file
        file.transferTo(dest)

        val fileUrl = "https://soulapp.my.id/assets/certificates/$fileName"

        return ResponseEntity.ok(
            mapOf(
                "fileName" to fileName,
                "url" to fileUrl
            )
        )
    }
    @PostMapping("/profile",consumes = ["multipart/form-data"])
    fun uploadProfile(
        @Parameter(
            description = "File to upload",
            required = true,
            content = [Content(mediaType = "multipart/form-data")]
        )
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {

        if (file.isEmpty) {
            throw RuntimeException("File is empty")
        }
        val fileName = System.currentTimeMillis().toString() + "_" + file.originalFilename

        val dest = File("$uploadDir/profiles/$fileName")

        dest.parentFile.mkdirs()

        file.transferTo(dest)

        val fileUrl = "https://soulapp.my.id/assets/profiles/$fileName"

        return ResponseEntity.ok(
            mapOf(
                "fileName" to fileName,
                "url" to fileUrl
            )
        )
    }
}