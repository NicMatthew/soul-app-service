package com.soul.app.soul_app_service.controller

import com.soul.app.soul_app_service.dto.response.GetAllPsychologyResponse
import com.soul.app.soul_app_service.dto.response.GetPsychologyDetailResponse
import com.soul.app.soul_app_service.dto.response.RatingAppResponse
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.service.AppointmentService
import com.soul.app.soul_app_service.service.PsychologyService
import com.soul.app.soul_app_service.service.RatingAppService
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
    private val psychologyService: PsychologyService,
    private val appointmentService: AppointmentService,
    private val ratingAppService: RatingAppService
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
            slots = appointmentService.getMonthlyAvailabilityWithStatus(psychologyId),
        ))
    }
    @GetMapping("/psychology")
    @Operation(
        summary = "Get All Psycholog",
    )
    private fun getAllPyschology(
        @RequestParam(name = "search") search: String?,
        @RequestParam(name = "rate") rate: String?,
        @RequestParam(name = "career") career: String?,
        @RequestParam(name = "price") price: String?
    ): ResponseEntity<List<GetAllPsychologyResponse>> {
        return ResponseEntity.ok(psychologyService.getAllPsychologies(search,rate,price, career ))
    }

    @GetMapping("/helper")
    @Operation(
        summary = "Get All Psycholog",
    )
    private fun getPsychologDetails(
        @RequestParam(name = "userId") userId: Int?,
        @RequestParam(name = "profileId") profileId: Int?
    ): ResponseEntity<Psychology?> {
        return ResponseEntity.ok(psychologyService.helper(userId, profileId))
    }

    @GetMapping("/rating-app")
    @Operation(
        summary = "Get All App Rating",
    )
    private fun getAllRatingApp(
    ): ResponseEntity<List<RatingAppResponse?>?> {
        return ResponseEntity.ok(ratingAppService.getAllRatingApp())
    }


}