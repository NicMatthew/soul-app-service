package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.request.SignUpPsychologyRequest
import com.soul.app.soul_app_service.model.Field
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyProfile
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val psychologyRepository: PsychologyRepository
) {
    fun signUpPyschology(signUpPsychologyRequest: SignUpPsychologyRequest): Psychology{
        val userId = userRepository.saveUser(User(
            id = -99,
            email = signUpPsychologyRequest.email,
            name = signUpPsychologyRequest.name,
            phone = signUpPsychologyRequest.phone,
            dob = signUpPsychologyRequest.dob,
            gender = signUpPsychologyRequest.gender,
            role = "psycholog",
            username = signUpPsychologyRequest.username,
            password_hash = signUpPsychologyRequest.password_hash


        ))
        val profileId = psychologyRepository.savePsychologyProfile(PsychologyProfile(
            id = -99,
            userId = userId,
            alumnus = signUpPsychologyRequest.alumnus,
            sipp = signUpPsychologyRequest.sipp,
            careerStartDate = signUpPsychologyRequest.careerStartDate,
            pricePerSession = signUpPsychologyRequest.pricePerSession,
            education = signUpPsychologyRequest.education,
            clinic = signUpPsychologyRequest.clinic,
            description = signUpPsychologyRequest.description,
            rating = 0F

        ))
        signUpPsychologyRequest.field_id.forEach { field ->
            psychologyRepository.savePsychologyField(profileId, field)
        }


        return Psychology(
            user = userRepository.getUserById(userId)!!,
            psychologyProfile = psychologyRepository.getPsychologyProfilebyUserId(userId)!!,
            fields = psychologyRepository.getPsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)

        )
    }


    fun getAllFields(): List<Field> {
        return psychologyRepository.getAllFields()
    }
}