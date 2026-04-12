package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.TimeSlot
import com.soul.app.soul_app_service.dto.TimeSlotWithStatus
import com.soul.app.soul_app_service.dto.request.CreatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfilePsychologyRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfileRequest
import com.soul.app.soul_app_service.dto.response.GetAllPsychologyResponse
import com.soul.app.soul_app_service.model.Field
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyAvailability
import com.soul.app.soul_app_service.model.PsychologyProfile
import com.soul.app.soul_app_service.repository.AppointmentRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.data.relational.core.sql.In
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
){
    fun createPsychologyAvailibility(psychologyAvailability: List<CreatePsychologyAvailabilityRequest>, userId: Int):List<PsychologyAvailability>{
        val psychologyId = psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!
        psychologyRepository.deleteAllPsychologyAvailability(psychologyId)
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
        val profileId = psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!
        return Psychology(
            userRepository.getUserById(userId)!!,
            psychologyRepository.getPsychologyProfilebyUserId(userId)!!,
            psychologyRepository.getPsychologyFields(profileId),
            certificates = psychologyRepository.getPsychologyCertificatesByPsychologyProfileId(profileId),
            ratings = psychologyRepository.getPsychologyRating(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)

        )
    }


    fun getAllPsychologies(
        search: String?,
        rate: String?,
        price: String?,
        experience: String?
    ): List<GetAllPsychologyResponse> {

        val rows = psychologyRepository.getPsychologyBase(search, rate, price, experience)

        return rows.mapNotNull { row ->
            val userId = row["id"] as Int
            val profileId = row["profile_id"] as Int

            val user = userRepository.getUserById(userId) ?: return@mapNotNull null
            val profile = psychologyRepository.getPsychologyProfilebyUserId(userId) ?: return@mapNotNull null

            GetAllPsychologyResponse(
                id = row["id"] as Int,
                user
            )
        }
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
            religion = request.religion
        ))
        psychologyRepository.replacePsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!, request.fields)
        return getPsychologyDetailByUserId(userId)!!
    }

    fun getUserIdFromPscyhologProfileId(psycholgProfileId : Int) : Int?{
        return psychologyRepository.getUserIdFromPscyhologProfileId(psycholgProfileId)
    }
    fun getAllFields(): List<Field> {
        return psychologyRepository.getAllFields()
    }
    fun helper(userId : Int?,profileId: Int?) : Psychology? {
        if (userId != null){
            return Psychology(
                userRepository.getUserById(userId)!!,
                psychologyRepository.getPsychologyProfilebyUserId(userId)!!,
                psychologyRepository.getPsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!),
                certificates = psychologyRepository.getPsychologyCertificatesByPsychologyProfileId(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!),
                ratings = psychologyRepository.getPsychologyRating(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)

            )
        }

        if (profileId != null){

            return Psychology(
                userRepository.getUserById(psychologyRepository.getUserIdFromPscyhologProfileId(profileId)!!)!!,
                psychologyRepository.getPsychologyProfilebyUserId(psychologyRepository.getUserIdFromPscyhologProfileId(profileId)!!)!!,
                psychologyRepository.getPsychologyFields(profileId),
                certificates = psychologyRepository.getPsychologyCertificatesByPsychologyProfileId(profileId),
                ratings = psychologyRepository.getPsychologyRating(profileId)

            )
        }
        return null
    }

}