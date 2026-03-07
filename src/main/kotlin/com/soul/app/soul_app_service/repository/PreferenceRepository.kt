package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.model.PreferenceQuestion
import com.soul.app.soul_app_service.model.PreferenceQuestionOption
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class PreferenceRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun getAllQuestions(): List<PreferenceQuestion> {
        val sql = """
            SELECT * FROM preference_questions
            ORDER BY id
        """.trimIndent()

        return jdbcTemplate.query(sql, questionRowMapper())
    }

    fun getOptionsByQuestionId(questionId: Int): List<PreferenceQuestionOption> {
        val sql = """
            SELECT * FROM preference_question_options
            WHERE question_id = ?
            ORDER BY id
        """.trimIndent()

        return jdbcTemplate.query(sql, optionRowMapper(), questionId)
    }

    private fun questionRowMapper() = RowMapper { rs, _ ->
        PreferenceQuestion(
            id = rs.getInt("id"),
            code = rs.getString("code"),
            description = rs.getString("description"),
            weight = rs.getInt("weight")
        )
    }

    private fun optionRowMapper() = RowMapper { rs, _ ->
        PreferenceQuestionOption(
            id = rs.getInt("id"),
            questionId = rs.getInt("question_id"),
            code = rs.getString("code"),
            label = rs.getString("label"),
            weight = rs.getInt("weight")
        )
    }
}