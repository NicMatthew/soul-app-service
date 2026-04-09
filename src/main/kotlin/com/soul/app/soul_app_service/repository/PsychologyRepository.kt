package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.dto.request.AddPsychologyCertificateRequest
import com.soul.app.soul_app_service.dto.request.CreatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.dto.request.UpdatePsychologyAvailabilityRequest
import com.soul.app.soul_app_service.model.Field
import com.soul.app.soul_app_service.model.PsychologyAvailability
import com.soul.app.soul_app_service.model.PsychologyCertificate
import com.soul.app.soul_app_service.model.PsychologyProfile
import com.soul.app.soul_app_service.model.RatingAppointment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository

@Repository
class PsychologyRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun savePsychologyProfile(profile: PsychologyProfile): Int {
        val sql = """
            INSERT INTO psychologist_profile
            (user_id, alumnus, sipp, career_start_date, price_per_session, education, clinic, description, rating, religion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            profile.userId,
            profile.alumnus,
            profile.sipp,
            profile.careerStartDate,
            profile.pricePerSession,
            profile.education,
            profile.clinic,
            profile.description,
            profile.rating,
            profile.religion,
        )
    }
    fun updatePsychologyProfile(profile: PsychologyProfile): Int {
        val sql = """
        UPDATE psychologist_profile
        SET
            alumnus = ?,
            sipp = ?,
            career_start_date = ?,
            price_per_session = ?,
            education = ?,
            clinic = ?,
            description = ?,
            religion = ?
        WHERE user_id = ?
    """.trimIndent()

        return jdbcTemplate.update(
            sql,
            profile.alumnus,
            profile.sipp,
            profile.careerStartDate,
            profile.pricePerSession,
            profile.education,
            profile.clinic,
            profile.description,
            profile.religion,
            profile.userId
        )
    }

    fun savePsychologyField(psychologyId: Int, fieldId: Int): Int {
        val sql = """
            INSERT INTO psychologist_field (psychologist_id, field_id)
            VALUES (?, ?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            psychologyId,
            fieldId
        )!!
    }

    fun getPsychologyProfilebyUserId(userId: Int): PsychologyProfile? {
        val sql = """
            SELECT *
            FROM psychologist_profile
            WHERE user_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
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
                    religion = rs.getString("religion"),
                )
            },
            userId
        ).firstOrNull()
    }

    fun getPsychologyProfilebyProfileId(userId: Int): PsychologyProfile? {
        val sql = """
            SELECT *
            FROM psychologist_profile
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
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
                    religion = rs.getString("religion"),
                )
            },
            userId
        ).firstOrNull()
    }

    fun getAllFields(): List<Field> {
        val sql = "SELECT id, field_name FROM field"

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                Field(
                    fieldId = rs.getInt("id"),
                    fieldName = rs.getString("field_name")
                )
            }
        )
    }

    fun getPsychologyFields(psychologyId: Int): List<Field> {
        val sql = """
            SELECT f.id, f.field_name
            FROM psychologist_field pf
            JOIN field f ON f.id = pf.field_id
            WHERE pf.psychologist_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                Field(
                    fieldId = rs.getInt("id"),
                    fieldName = rs.getString("field_name")
                )
            },
            psychologyId
        )
    }

    fun getPsychologyProfileIdFromUserId(userId: Int): Int? {
        val sql = """
            SELECT id
            FROM psychologist_profile
            WHERE user_id = ?
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            userId
        )
    }

    fun getUserIdFromPscyhologProfileId(psychologProfileId: Int): Int? {
        val sql = """
            SELECT user_id
            FROM psychologist_profile
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            psychologProfileId
        )
    }

    fun savePsychologyAvailability(psychologyAvailability: CreatePsychologyAvailabilityRequest,psychologyId: Int): Int {
        val sql = """
            INSERT INTO psychologist_availability (psychologist_id, day_of_week,start_time,end_time)
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        return jdbcTemplate.update(
            sql,
            psychologyId,
            psychologyAvailability.dayOfWeek,
            psychologyAvailability.startTime,
            psychologyAvailability.endTime
        )
    }
    fun getPsychologyAvailability(psychologyid: Int): List<PsychologyAvailability>? {
        val sql = """
            SELECT *
            FROM psychologist_availability
            WHERE psychologist_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                PsychologyAvailability(
                    id = rs.getInt("id"),
                    psychologistId = rs.getInt("psychologist_id"),
                    dayOfWeek = rs.getInt("day_of_week"),
                    startTime = rs.getString("start_time"),
                    endTime = rs.getString("end_time")
                )
            },
            psychologyid
        )
    }
    fun updatePsychologyAvailability(
        psychologyId: Int,
        psychologyAvailability: UpdatePsychologyAvailabilityRequest
    ): Int {
        val sql = """
        UPDATE psychologist_availability
        SET day_of_week = ?,
            start_time = ?,
            end_time = ?
        WHERE id = ?
          AND psychologist_id = ?
    """.trimIndent()

        return jdbcTemplate.update(
            sql,
            psychologyAvailability.dayOfWeek,
            psychologyAvailability.startTime,
            psychologyAvailability.endTime,
            psychologyAvailability.id,
            psychologyId
        )
    }

fun deleteAllPsychologyAvailability(psychologyId: Int): Int {
        val deleteSql = """
        DELETE FROM psychologist_availability
        WHERE psychologist_id = ?
    """.trimIndent()

        return jdbcTemplate.update(deleteSql, psychologyId)
    }
    fun replacePsychologyFields(psychologyId: Int, fieldIds: List<Field>) {
        val deleteSql = """
        DELETE FROM psychologist_field
        WHERE psychologist_id = ?
    """.trimIndent()

        jdbcTemplate.update(deleteSql, psychologyId)
        val insertSql = """
        INSERT INTO psychologist_field (psychologist_id, field_id)
        VALUES (?, ?)
    """.trimIndent()

        fieldIds.forEach {
            jdbcTemplate.update(
                insertSql,
                psychologyId,
                it.fieldId
            )
        }

    }

    fun savePsychologyCertificate(certificates: AddPsychologyCertificateRequest): Int {
        val sql = """
        INSERT INTO psychologist_certificates (psychologist_id, path, created_at)
        VALUES (?, ?, NOW())
        RETURNING id
    """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            certificates.psychologyId,
            certificates.path
        )
    }
        fun deletePsychologyCertificate(psychologyId: Int,certificateId: Int): Int {
        val deleteSql = """
        DELETE FROM psychologist_certificates
        WHERE psychologist_id = ? AND id = ?
    """.trimIndent()

        return jdbcTemplate.update(deleteSql, psychologyId,certificateId)
    }
    fun getPsychologyCertificatesByPsychologyCertificateId(psychologyCertificateId: Int): PsychologyCertificate? {
        val sql = """
            SELECT *
            FROM psychologist_certificates
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                PsychologyCertificate(
                    id = rs.getInt("id"),
                    psychologyId = rs.getInt("psychologist_id"),
                    path = rs.getString("path"),
                )
            },
            psychologyCertificateId
        ).firstOrNull()
    }

    fun getPsychologyCertificatesByPsychologyProfileId(psychologyId: Int): List<PsychologyCertificate>? {
        val sql = """
            SELECT *
            FROM psychologist_certificates
            WHERE psychologist_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                PsychologyCertificate(
                    id = rs.getInt("id"),
                    psychologyId = rs.getInt("psychologist_id"),
                    path = rs.getString("path"),
                )
            },
            psychologyId
        )
    }
    fun getAllPsychologyProfileId(): List<Int> {
        return jdbcTemplate.query(
            "SELECT id FROM psychologist_profile",
            { rs, _ -> rs.getInt("id") }
        )
    }
    fun getPsychologyBase(
        search: String?,
        rate: String?,
        price: String?,
        experience: String?
    ): List<Map<String, Any>> {

        val sql = StringBuilder("""
            SELECT 
                u.id,
                u.name,
                u.email,
                p.id as profile_id,
                p.price_per_session,
                p.career_start_date,
                COALESCE(r.avg_rating, 0) as avg_rating
            FROM users u
            JOIN psychologist_profile p ON p.user_id = u.id
            LEFT JOIN (
                SELECT psychologist_id, AVG(rate) as avg_rating
                FROM rating
                GROUP BY psychologist_id
            ) r ON r.psychologist_id = p.id
            WHERE u.role = ?
            """)

        val params = mutableListOf<Any>("psycholog")

        if (!search.isNullOrBlank()) {
            sql.append(" AND u.name ILIKE ?")
            params.add("%$search%")
        }

            val orderClauses = mutableListOf<String>()

        if (!rate.isNullOrBlank()) {
            orderClauses.add("r.avg_rating ${if (rate.equals("desc", true)) "DESC" else "ASC"}")
        }

        if (!price.isNullOrBlank()) {
            orderClauses.add("p.price_per_session ${if (price.equals("desc", true)) "DESC" else "ASC"}")
        }

        if (!experience.isNullOrBlank()) {
            orderClauses.add("p.career_start_date ${if (experience.equals("desc", true)) "DESC" else "ASC"}")
        }

        if (orderClauses.isEmpty()) {
            orderClauses.add("avg_rating DESC")
        }

        sql.append(" ORDER BY ${orderClauses.joinToString(", ")} ")

        return jdbcTemplate.queryForList(sql.toString(), *params.toTypedArray())
    }

    fun getPsychologyRating(psychologyId: Int):List<RatingAppointment>{
        val sql = """
            SELECT * from rating where psychologist_id = ?
        """.trimIndent()
        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                RatingAppointment(
                    id = rs.getInt("id"),
                    psychologyId = rs.getInt("psychologist_id"),
                    appointmentId = rs.getInt("appointment_id"),
                    userId = rs.getInt("client_user_id"),
                    rate = rs.getInt("rate"),
                    description = rs.getString("description"),
                )
            },
            psychologyId
        )
    }



}
