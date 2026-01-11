package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.model.PsychologyProfile
import com.soul.app.soul_app_service.util.executePreparedStatementQuery
import com.soul.app.soul_app_service.util.getConnectionOrThrows
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class PsychologyRepository(private val jdbcTemplate: JdbcTemplate) {
    fun savePsychologyProfile(profile: PsychologyProfile): String {
        val query =
            "INSERT INTO public.psychologist_profile (user_id, alumnus,sipp, career_start_date, price_per_session, education, clinic, description,rating) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?) RETURNING id"
        return executePreparedStatementQuery(
            jdbcTemplate.getConnectionOrThrows(),
            query,
            {
                setInt(1, profile.userId)
                setString(2, profile.alumnus)
                setString(3, profile.sipp)
                setDate(4, profile.careerStartDate)
                setInt(5, profile.pricePerSession)
                setString(6, profile.education)
                setString(7, profile.clinic)
                setString(8, profile.description)
                setFloat(9, profile.rating)
            },
            RowMapper { rs, _ ->
                val id = rs.getInt("id")
                return@RowMapper "$id"
            }
        ).first()
    }

    fun getPsychologyProfile(userId: Int): PsychologyProfile? {
        val query = "SELECT * FROM public.psychologist_profile WHERE user_id = ?"
        return jdbcTemplate.queryForObject(
            query,
            RowMapper { rs, _ ->
                PsychologyProfile(
                    id = rs.getInt("id"),
                    userId = rs.getInt("user_id"),
                    alumnus = rs.getString("alumnus"),
                    sipp = rs.getString("sipp"),
                    careerStartDate = rs.getDate("career_start_date"),
                    pricePerSession = rs.getInt("price_per_session"),
                    education = rs.getString("education"),
                    clinic = rs.getString("clinic"),
                    description = rs.getString("description"),
                    rating = rs.getFloat("rating"),
                )
            },
            userId
        )
    }
}