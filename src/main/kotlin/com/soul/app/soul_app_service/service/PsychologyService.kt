package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.TimeSlot
import com.soul.app.soul_app_service.dto.TimeSlotWithStatus
import com.soul.app.soul_app_service.dto.request.CreatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfilePsychologyRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfileRequest
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyAvailability
import com.soul.app.soul_app_service.model.PsychologyProfile
import com.soul.app.soul_app_service.repository.AppointmentRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class PsychologyService (
    private val psychologyRepository: PsychologyRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val appointmentRepository: AppointmentRepository
){
    fun createPsychologyAvailibility(psychologyAvailability: List<CreatePsychologyAvailabilityRequest>, userId: Int):List<PsychologyAvailability>{
        val psychologyId = psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!
        psychologyAvailability.forEach {
            psychologyRepository.savePsychologyAvailability(
                CreatePsychologyAvailabilityRequest(
                    dayOfWeek = it.dayOfWeek,
                    startTime = it.startTime,
                    endTime = it.endTime,
                ),
                psychologyId
            )
        }
        return getPsychologyAvailibility(userId)!!
    }
    fun getPsychologyAvailibility(userId: Int): List<PsychologyAvailability>?{
        return psychologyRepository.getPsychologyAvailability(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)
    }
    fun getPsychologyDetailByUserId(userId: Int): Psychology?{
        return Psychology(
            userRepository.getUserById(userId)!!,
            psychologyRepository.getPsychologyProfilebyUserId(userId)!!,
            psychologyRepository.getPsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)
        )
    }


    fun getAllPsychologies(search : String?): List<Psychology> {
        val users = userRepository.getAllPsychologyUser(search)
        var psychologies = mutableListOf<Psychology>()
        users.forEach {
            psychologies.add(
                Psychology(
                    it,
                    psychologyRepository.getPsychologyProfilebyUserId(it.id)!!,
                    psychologyRepository.getPsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(it.id)!!)
                )
            )
        }
        return psychologies
    }
    fun updateProfile(userId: Int, request: UpdateProfilePsychologyRequest): Psychology{
        userService.updateProfile(userId, UpdateProfileRequest(
            username = request.username,
            phone = request.phone,
            dob = request.dob,
            gender = request.gender,
            profilePicture = request.profile_picture,
            anonymous = false
        ))
        psychologyRepository.updatePsychologyProfile(PsychologyProfile(
            userId = userId,
            id = -99,
            alumnus = request.alumnus,
            sipp = request.sipp,
            careerStartDate = request.careerStartDate,
            pricePerSession = request.pricePerSession,
            education = request.education,
            clinic = request.clinic,
            description = request.description,
        ))
        psychologyRepository.replacePsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!, request.fields)
        return getPsychologyDetailByUserId(userId)!!
    }

    fun getUserIdFromPscyhologProfileId(psycholgProfileId : Int) : Int?{
        return psychologyRepository.getUserIdFromPscyhologProfileId(psycholgProfileId)
    }

}