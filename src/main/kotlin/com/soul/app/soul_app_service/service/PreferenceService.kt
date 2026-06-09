package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.RecommendationResult
import com.soul.app.soul_app_service.dto.UserAnswer
import com.soul.app.soul_app_service.dto.request.PreferenceAnswerRequest
import com.soul.app.soul_app_service.dto.response.PreferenceOptionResponse
import com.soul.app.soul_app_service.dto.response.PreferenceQuestionResponse
import com.soul.app.soul_app_service.dto.response.RecommendationResultResponse
import com.soul.app.soul_app_service.repository.PreferenceRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import org.springframework.stereotype.Service
import kotlin.collections.forEach

@Service
class PreferenceService(
    private val preferenceRepository: PreferenceRepository,
    private val psychologyRepository: PsychologyRepository
) {

    fun getAllUserQuestions(): List<PreferenceQuestionResponse> {

        val questions = preferenceRepository.getAllQuestions("CLIENT")

        return questions.map { question ->

            val options = preferenceRepository
                .getOptionsByQuestionId(question.id)
                .map {
                    PreferenceOptionResponse(
                        id = it.id,
                        label = it.label
                    )
                }

            PreferenceQuestionResponse(
                id = question.id,
                question = question.description,
                required = question.required,
                multipleAnswer = question.multipleAnswer,
                options = options
            )
        }
    }
    fun submitPreferenceUser(userId:Int,answer:List<PreferenceAnswerRequest>): String{
        preferenceRepository.submitUserAnswers(userId,answer)
        return "mantap"
    }

    fun submitPreferencePsycholog(userId:Int,answer:List<PreferenceAnswerRequest>): String{
        preferenceRepository.submitPsychologAnswers(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!,answer)
        return "mantap"
    }
    fun getAllPsychologistQuestions(): List<PreferenceQuestionResponse> {

        val questions = preferenceRepository.getAllQuestions("PSYCHOLOGIST")

        return questions.map { question ->

            val options = preferenceRepository
                .getOptionsByQuestionId(question.id)
                .map {
                    PreferenceOptionResponse(
                        id = it.id,
                        label = it.label
                    )
                }

            PreferenceQuestionResponse(
                id = question.id,
                question = question.description,
                required = question.required,
                multipleAnswer = question.multipleAnswer,
                options = options
            )
        }
    }
    fun getRecommendations(userId: Int): List<RecommendationResultResponse> {
        val recommendation = preferenceRepository.getUserRecommendation(userId)
        if (!recommendation.isNullOrEmpty()) {
            val response = mutableListOf<RecommendationResultResponse>()
            recommendation.forEach { recommendation ->
                val base = psychologyRepository.getPsychologyBaseByProfileId(recommendation.psychologistId)!!
                response.add(RecommendationResultResponse(
                    profileId = recommendation.psychologistId,
                    userId = base.userId,
                    name = base.name,
                    profilePicture = base.profilePicture,
                    rating = base.rating,
                    careerStartDate = base.careerStartDate,
                    description = base.description,
                    pricePerSession = base.pricePerSession,
                    score = recommendation.score,
                    reasons = recommendation.reasons
                ))

            }
            return response
        }
        return calculateAndCache(userId)
    }

    fun calculateAndCache(userId: Int): List<RecommendationResultResponse> {
        val userAnswers = preferenceRepository.getUserAnswers(userId)
        if (userAnswers.isEmpty()) {
            throw IllegalArgumentException("Please submit user answers first")
        }

        val psychologists = psychologyRepository.getAllPsychologyProfileId()
        if (psychologists.isEmpty()) return emptyList()

        val scores = mutableMapOf<Int, Pair<Double, String>>()
        psychologists.forEach { psychologistId ->
            val result = calculateScore( psychologistId, userAnswers)
            scores[psychologistId] = Pair(result.first, result.second)
        }


        preferenceRepository.saveRecommendation(userId, scores)
        return getRecommendations(userId)
    }

    private fun calculateScore(
        psychologistId: Int,
        userAnswers: List<UserAnswer>
    ): Pair<Double, String> {

        var totalScore = 0.0
        var maxScore = 0.0

        val matchedReasons = mutableSetOf<String>()

        val psychGender =
            preferenceRepository.getPsychologistGender(psychologistId)?.uppercase()

        val psychReligion =
            preferenceRepository.getPsychologistReligion(psychologistId)

        val psychExperience =
            preferenceRepository.getPsychologistExperienceYears(psychologistId)

        val psychFields =
            preferenceRepository.getPsychologistFields(psychologistId)
                .map { it.uppercase() }
                .toSet()

        val psychLgbt =
            preferenceRepository.getPsychologistPreferenceAnswer(
                psychologistId,
                "psychologist_lgbt"
            )

        val psychMedical =
            preferenceRepository.getPsychologistPreferenceAnswers(
                psychologistId,
                "psychologist_medical"
            ).toSet()

        val psychSeverity =
            preferenceRepository.getPsychologistPreferenceAnswer(
                psychologistId,
                "psychologist_severity"
            )

        userAnswers.forEach { answer ->
            val score = answer.questionWeight * answer.optionWeight
            if (answer.optionCode != "NO_PREFERENCE") {
                maxScore += score
            }

            when (answer.questionCode) {
                "psychologist_gender" -> {
                    if (answer.optionCode != "NO_PREFERENCE" && psychGender == answer.optionCode.uppercase()
                    ) {
                        totalScore += score
                        matchedReasons.add("Gender sesuai preferensi")
                    }
                }
                "religion_preference" -> {
                    if (answer.optionCode != "NO_PREFERENCE" && psychReligion == answer.optionCode) {
                        totalScore += score
                        matchedReasons.add("Agama sesuai preferensi")
                    }
                }
                "experience_preference" -> {
                    val match = when (answer.optionCode) {
                        "EXP_1_3" -> psychExperience >= 1
                        "EXP_3_5" -> psychExperience >= 3
                        "EXP_5_PLUS" -> psychExperience >= 5
                        else -> false
                    }
                    if (match) {
                        totalScore += score
                        matchedReasons.add("Pengalaman sesuai preferensi")
                    }
                }
                "problem_area" -> {
                    if (psychFields.contains(answer.optionCode)) {
                        totalScore += score
                        matchedReasons.add("Spesialisasi sesuai masalah")
                    }
                }
                "client_lgbt" -> {
                    if (answer.optionCode == "YES" && psychLgbt == "YES") {
                        totalScore += score
                        matchedReasons.add("Psikolog terbuka terhadap LGBT")
                    }
                }
                "medical_condition" -> {
                    if (answer.optionCode != "NONE" && psychMedical.contains(answer.optionCode)) {
                        totalScore += score
                        matchedReasons.add("Psikolog dapat menangani kondisi medis")
                    }
                }

                "severity_level" -> {

                    val match = when (answer.optionCode) {
                        "MILD" -> psychSeverity == "NO"
                        "SEVERE" -> psychSeverity == "YES"
                        else -> false
                    }

                    if (match) {
                        totalScore += score

                        if (
                            answer.optionCode == "SEVERE" &&
                            psychSeverity == "YES"
                        ) {
                            matchedReasons.add("Berpengalaman menangani kasus berat")
                        }
                    }
                }
            }
        }

        val normalizedScore =
            if (maxScore > 0)
                (totalScore / maxScore) * 100
            else
                0.0

        return Pair(
            normalizedScore,
            matchedReasons.joinToString(", ")
        )
    }
}