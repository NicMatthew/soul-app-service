package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.RecommendationResult
import com.soul.app.soul_app_service.dto.UserAnswer
import com.soul.app.soul_app_service.dto.request.PreferenceAnswerRequest
import com.soul.app.soul_app_service.dto.response.PreferenceOptionResponse
import com.soul.app.soul_app_service.dto.response.PreferenceQuestionResponse
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
                options = options
            )
        }
    }
    fun getRecommendations(userId: Int): List<RecommendationResult> {
        val recommendation = preferenceRepository.getUserRecommendation(userId)
        if (!recommendation.isNullOrEmpty()) {
            return recommendation
        }
        return calculateAndCache(userId)
    }

    fun calculateAndCache(userId: Int): List<RecommendationResult> {
        // 1. Ambil jawaban user
        val userAnswers = preferenceRepository.getUserAnswers(userId)
        if (userAnswers.isEmpty()) {
            throw IllegalArgumentException("Please submiot user answers first")
        }

        // 2. Ambil semua psikolog
        val psychologists = psychologyRepository.getAllPsychologyProfileId()
        if (psychologists.isEmpty()) return emptyList()

        // 3. Hitung skor tiap psikolog
        val scores = mutableMapOf<Int, Pair<Double, String>>()
        psychologists.forEach { psychologistId ->
            val result = calculateScore(userId, psychologistId, userAnswers)
            scores[psychologistId] = Pair(result.first, result.second)
        }

        val maxScore = scores.values.maxOfOrNull { it.first } ?: 1.0
        val normalizedScores = scores.mapValues { (_, value) ->
            Pair((value.first / maxScore) * 100, value.second)
        }

        preferenceRepository.saveRecommendation(userId, normalizedScores)
        return preferenceRepository.getUserRecommendation(userId)!!
    }

    private fun calculateScore(
        userId: Int,
        psychologistId: Int,
        userAnswers: List<UserAnswer>
    ): Pair<Double, String> {
        var totalScore = 0.0
        val matchedReasons = mutableListOf<String>()

        userAnswers.forEach { answer ->
            when (answer.questionCode) {

                // Match: gender
                "psychologist_gender" -> {
                    val psychGender = preferenceRepository.getPsychologistGender(psychologistId)
                    if (answer.optionCode == "NO_PREFERENCE" ||
                        psychGender?.uppercase() == answer.optionCode.uppercase()) {
                        totalScore += answer.questionWeight * answer.optionWeight
                        if (answer.optionCode != "NO_PREFERENCE")
                            matchedReasons.add("Gender sesuai preferensi")
                    }
                }

                // Match: religion
                "religion_preference" -> {
                    val psychReligion = preferenceRepository.getPsychologistReligion(psychologistId)
                    if (answer.optionCode == "NO_PREFERENCE" ||
                        psychReligion == answer.optionCode) {
                        totalScore += answer.questionWeight * answer.optionWeight
                        if (answer.optionCode != "NO_PREFERENCE")
                            matchedReasons.add("Agama sesuai preferensi")
                    }
                }

                // Match: experience (career_start_date)
                "experience_preference" -> {
                    val yearsExp = preferenceRepository.getPsychologistExperienceYears(psychologistId)
                    val match = when (answer.optionCode) {
                        "EXP_1_3"       -> yearsExp in 1..3
                        "EXP_3_5"       -> yearsExp in 3..5
                        "EXP_5_PLUS"    -> yearsExp > 5
                        "NO_PREFERENCE" -> true
                        else -> false
                    }
                    if (match) {
                        totalScore += answer.questionWeight * answer.optionWeight
                        if (answer.optionCode != "NO_PREFERENCE")
                            matchedReasons.add("Pengalaman sesuai preferensi")
                    }
                }

                // Match: problem_area → field psikolog
                "problem_area" -> {
                    val psychFields = preferenceRepository.getPsychologistFields(psychologistId)
                    if (psychFields.any { it.uppercase() == answer.optionCode.uppercase() }) {
                        totalScore += answer.questionWeight * answer.optionWeight
                        matchedReasons.add("Spesialisasi sesuai masalah")
                    }
                }

                // Match: lgbt
                "client_lgbt" -> {
                    val psychLgbt = preferenceRepository.getPsychologistPreferenceAnswer(psychologistId, "psychologist_lgbt")
                    if (answer.optionCode == "NO_PREFERENCE" || answer.optionCode == "NO") {
                        totalScore += answer.questionWeight * answer.optionWeight
                    } else if (answer.optionCode == "YES" && psychLgbt == "YES") {
                        totalScore += answer.questionWeight * answer.optionWeight
                        matchedReasons.add("Psikolog terbuka terhadap LGBT")
                    }
                }

                // Match: medical_condition
                "medical_condition" -> {
                    if (answer.optionCode == "NONE") {
                        totalScore += answer.questionWeight * answer.optionWeight
                    } else {
                        val psychMedical = preferenceRepository.getPsychologistPreferenceAnswers(psychologistId, "psychologist_medical")
                        if (psychMedical.contains(answer.optionCode)) {
                            totalScore += answer.questionWeight * answer.optionWeight
                            matchedReasons.add("Psikolog bisa menangani kondisi medis kamu")
                        }
                    }
                }

                "severity_level" -> {
                    val psychSeverity = preferenceRepository.getPsychologistPreferenceAnswer(psychologistId, "psychologist_severity")
                    val match = when (answer.optionCode) {
                        "MILD", "MODERATE" -> true // semua psikolog bisa handle
                        "SEVERE" -> psychSeverity == "YES"
                        else -> false
                    }
                    if (match) {
                        totalScore += answer.questionWeight * answer.optionWeight
                        if (answer.optionCode == "SEVERE" && psychSeverity == "YES")
                            matchedReasons.add("Psikolog berpengalaman menangani kasus berat")
                    }
                }
            }
        }

        return Pair(totalScore, matchedReasons.joinToString(", "))
    }
}