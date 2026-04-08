package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.request.AddPsychologyCertificateRequest
import com.soul.app.soul_app_service.dto.request.DeletePsychologyCertificateRequest
import com.soul.app.soul_app_service.dto.request.SignUpPsychologyRequest
import com.soul.app.soul_app_service.model.Field
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyCertificate
import com.soul.app.soul_app_service.model.PsychologyProfile
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val psychologyRepository: PsychologyRepository,
    private val conventionErrorViewResolver: DefaultErrorViewResolver
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
            password_hash = "password",
            anonymous = false //todo generate password
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
            rating = 0F,
            religion = signUpPsychologyRequest.religion,

        ))
        signUpPsychologyRequest.fieldId.forEach { field ->
            psychologyRepository.savePsychologyField(profileId, field)
        }


        return Psychology(
            user = userRepository.getUserById(userId)!!,
            psychologyProfile = psychologyRepository.getPsychologyProfilebyUserId(userId)!!,
            fields = psychologyRepository.getPsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!),
            certificates = psychologyRepository.getPsychologyCertificatesByPsychologyProfileId(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!),
            ratings = psychologyRepository.getPsychologyRating(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)

        )
    }



    fun deletePsychologyAccount(userId: Int): String {
        userRepository.deleteUser(userId)
        return "Psychology with id ${userId} deleted successfully"
    }

    fun addCertificate(certificate: AddPsychologyCertificateRequest): PsychologyCertificate {
        val id = psychologyRepository.savePsychologyCertificate(certificate)

        return psychologyRepository.getPsychologyCertificatesByPsychologyCertificateId(id)!!
    }
    fun deleteCertificate(certificate: DeletePsychologyCertificateRequest): String {

        psychologyRepository.deletePsychologyCertificate(certificate.psychologyId, certificate.certificateId)
        return "Psychology deleted successfully"
    }
}