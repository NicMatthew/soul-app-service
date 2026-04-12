package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.dto.RecommendationResult
import com.soul.app.soul_app_service.dto.UserAnswer
import com.soul.app.soul_app_service.dto.request.PreferenceAnswerRequest
import com.soul.app.soul_app_service.model.PreferenceQuestion
import com.soul.app.soul_app_service.model.PreferenceQuestionOption
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class PreferenceRepository(
     val jdbcTemplate: JdbcTemplate
) {

    fun getAllQuestions(target : String): List<PreferenceQuestion> {
        val sql = """
            SELECT * FROM preference_questions WHERE target = ?
            ORDER BY id
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                PreferenceQuestion(
                    id = rs.getInt("id"),
                    code = rs.getString("code"),
                    description = rs.getString("description"),
                    weight = rs.getInt("weight")
                )
            },
            target

        )
    }
    fun submitUserAnswers(userId: Int, answers: List<PreferenceAnswerRequest>) {
        jdbcTemplate.update("DELETE FROM user_preference_answers WHERE user_id = ?", userId)
        jdbcTemplate.update("DELETE FROM recommendations WHERE user_id = ?", userId)

        val sql = """
            INSERT INTO user_preference_answers (user_id, question_id, option_id)
            VALUES (?, ?, ?)
        """.trimIndent()

        answers.forEach { answer ->
            jdbcTemplate.update(sql, userId, answer.questionId, answer.optionId)
        }
    }

    fun submitPsychologAnswers(profileId: Int, answers: List<PreferenceAnswerRequest>) {
        jdbcTemplate.update("DELETE FROM psychologist_preference_answers  WHERE psychologist_id  = ?", profileId)

        val sql = """
            INSERT INTO psychologist_preference_answers (psychologist_id , question_id, option_id)
            VALUES (?, ?, ?)
        """.trimIndent()

        answers.forEach { answer ->
            jdbcTemplate.update(sql, profileId, answer.questionId, answer.optionId)
        }
    }


    fun getOptionsByQuestionId(questionId: Int): List<PreferenceQuestionOption> {
        val sql = """
            SELECT * FROM preference_question_options
            WHERE question_id = ?
            ORDER BY id
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                PreferenceQuestionOption(
                    id = rs.getInt("id"),
                    questionId = rs.getInt("question_id"),
                    code = rs.getString("code"),
                    label = rs.getString("label"),
                    weight = rs.getInt("weight")
                )
            },
            questionId
        )
    }
    fun getUserRecommendation(userId: Int): List<RecommendationResult>? {
        val sql = """
            SELECT psychologist_id, score, reasons
            FROM recommendations
            WHERE user_id = ?
            ORDER BY score DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            RecommendationResult(
                psychologistId = rs.getInt("psychologist_id"),
                score = rs.getDouble("score"),
                reasons = rs.getString("reasons")
            )
        }, userId)
    }
    fun getUserAnswers(userId: Int): List<UserAnswer> {
        val sql = """
            SELECT 
                upa.question_id,
                upa.option_id,
                pq.code AS question_code,
                pq.weight AS question_weight,
                pqo.code AS option_code,
                pqo.weight AS option_weight
            FROM user_preference_answers upa
            JOIN preference_questions pq ON pq.id = upa.question_id
            JOIN preference_question_options pqo ON pqo.id = upa.option_id
            WHERE upa.user_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            UserAnswer(
                questionId = rs.getInt("question_id"),
                optionId = rs.getInt("option_id"),
                questionCode = rs.getString("question_code"),
                questionWeight = rs.getDouble("question_weight"),
                optionCode = rs.getString("option_code"),
                optionWeight = rs.getDouble("option_weight")
            )
        }, userId)
    }
    fun saveRecommendation(userId: Int, scores: Map<Int, Pair<Double, String>>) {
        jdbcTemplate.update("DELETE FROM recommendations WHERE user_id = ?", userId)

        val sql = """
            INSERT INTO recommendations (user_id, psychologist_id, score, reasons, created_at)
            VALUES (?, ?, ?, ?, NOW())
        """.trimIndent()

        scores.forEach { (psychologistId, scoreAndReason) ->
            jdbcTemplate.update(sql, userId, psychologistId, scoreAndReason.first, scoreAndReason.second)
        }
    }
    fun getPsychologistGender(psychologistId: Int): String? {
        return jdbcTemplate.queryForObject(
            """
            SELECT u.gender FROM psychologist_profile pp
            JOIN users u ON u.id = pp.user_id
            WHERE pp.id = ?
            """.trimIndent(),
            String::class.java, psychologistId
        )
    }

     fun getPsychologistReligion(psychologistId: Int): String? {
        return jdbcTemplate.queryForObject(
            "SELECT religion FROM psychologist_profile WHERE id = ?",
            String::class.java, psychologistId
        )
    }

     fun getPsychologistExperienceYears(psychologistId: Int): Int {
        return jdbcTemplate.queryForObject(
            """
            SELECT EXTRACT(YEAR FROM AGE(NOW(), career_start_date))::int 
            FROM psychologist_profile WHERE id = ?
            """.trimIndent(),
            Int::class.java, psychologistId
        ) ?: 0
    }

     fun getPsychologistFields(psychologistId: Int): List<String> {
        return jdbcTemplate.query(
            """
            SELECT f.field_name FROM psychologist_field pf
            JOIN field f ON f.id = pf.field_id
            WHERE pf.psychologist_id = ?
            """.trimIndent(),
            { rs, _ -> rs.getString("field_name") },
            psychologistId
        )
    }

     fun getPsychologistPreferenceAnswer(psychologistId: Int, questionCode: String): String? {
        return runCatching {
            jdbcTemplate.queryForObject(
                """
                SELECT pqo.code FROM psychologist_preference_answers ppa
                JOIN preference_question_options pqo ON pqo.id = ppa.option_id
                JOIN preference_questions pq ON pq.id = ppa.question_id
                WHERE ppa.psychologist_id = ? AND pq.code = ?
                """.trimIndent(),
                String::class.java, psychologistId, questionCode
            )
        }.getOrNull()
    }

     fun getPsychologistPreferenceAnswers(psychologistId: Int, questionCode: String): List<String> {
        return jdbcTemplate.query(
            """
            SELECT pqo.code FROM psychologist_preference_answers ppa
            JOIN preference_question_options pqo ON pqo.id = ppa.option_id
            JOIN preference_questions pq ON pq.id = ppa.question_id
            WHERE ppa.psychologist_id = ? AND pq.code = ?
            """.trimIndent(),
            { rs, _ -> rs.getString("code") },
            psychologistId, questionCode
        )
    }


}