package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.SignUpPsychologyRequest
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
            role = "psychology",
            username = signUpPsychologyRequest.username,
            password_hash = signUpPsychologyRequest.password_hash


        ))
        psychologyRepository.savePsychologyProfile(PsychologyProfile(
            id = -99,
            userId = userId.toInt(),
            alumnus = signUpPsychologyRequest.alumnus,
            sipp = signUpPsychologyRequest.sipp,
            careerStartDate = signUpPsychologyRequest.careerStartDate,
            pricePerSession = signUpPsychologyRequest.pricePerSession,
            education = signUpPsychologyRequest.education,
            clinic = signUpPsychologyRequest.clinic,
            description = signUpPsychologyRequest.description,
            rating = 0F

        ))

        return Psychology(
            user = userRepository.getUserById(userId.toInt())!!,
            psychologyProfile = psychologyRepository.getPsychologyProfile(userId.toInt())!!,
        )
    }
    fun getAllPsychologies(): List<Psychology> {
        val users = userRepository.getAllPsychologyUser()
        var psychologies = mutableListOf<Psychology>()
        users?.forEach {
            psychologies.add(
                Psychology(
                    it,
                    psychologyRepository.getPsychologyProfile(it.id)!!,
                )
            )
        }
        return psychologies
    }
}