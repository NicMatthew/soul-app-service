package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.request.CreatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.model.Psychology
import com.soul.app.soul_app_service.model.PsychologyAvailability
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class PsychologyService (
    private val psychologyRepository: PsychologyRepository,
    private val userRepository: UserRepository
){
    fun createPsychologyAvailibility(psychologyAvailability: List<CreatePsychologyAvailabilityRequest>, userId: Int){
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
    }
    fun getPsychologyAvailibility(userId: Int): List<PsychologyAvailability>?{
        return psychologyRepository.getPsychologyAvailability(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)
    }
    fun getPsychologyDetailByUserId(userId: Int): Psychology?{
        return Psychology(
            userRepository.getUserById(userId)!!,
            psychologyRepository.getPsychologyProfile(userId)!!,
            psychologyRepository.getPsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)
        )
    }
    fun getAllPsychologies(): List<Psychology> {
        val users = userRepository.getAllPsychologyUser()
        var psychologies = mutableListOf<Psychology>()
        users.forEach {
            psychologies.add(
                Psychology(
                    it,
                    psychologyRepository.getPsychologyProfile(it.id)!!,
                    psychologyRepository.getPsychologyFields(psychologyRepository.getPsychologyProfileIdFromUserId(it.id)!!)
                )
            )
        }
        return psychologies
    }
}