package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.dto.request.CreateAppointmentRequest
import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.AppointmentSlot
import com.soul.app.soul_app_service.model.PsychologyAvailability
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
class AppointmentRepository(
    private val jdbcTemplate: JdbcTemplate

) {
    fun createAppointment(userId:Int,request: CreateAppointmentRequest,status: String): Int {
        val sql = """
            INSERT INTO appointments
            (client_user_id, psychologist_id, status, start_time, end_time)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            userId,
            request.psychologyId,
            status,
            request.startTime,
            request.endTime
        )
    }
    fun getAppointmentById(appointmentId: Int): Appointment? {
        val sql = """
        SELECT *
        FROM appointments
        WHERE id = ?
    """.trimIndent()

        return jdbcTemplate.query(
            sql,
            { rs, _ ->
                Appointment(
                    id = rs.getInt("id"),
                    clientUserId = rs.getInt("client_user_id"),
                    psychologyId = rs.getInt("psychologist_id"),
                    scheduledAt = rs.getString("scheduled_at"),
                    startTime = rs.getString("start_time"),
                    endTime = rs.getString("end_time"),
                    status = rs.getString("status"),
                    notesPsychology = rs.getString("notes_psychologist"),
                    description = rs.getString("description"),
                )
            },
            appointmentId
        ).firstOrNull()
    }


    fun createAppointmentSlot(appointmentSlot: AppointmentSlot): Int {
        val sql = """
            INSERT INTO appointment_slots
            ( psychologist_id, date,status, start_time, end_time,appointment_id)
            VALUES (?, ?, ?, ?, ?,?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            appointmentSlot.psychologyId,
            appointmentSlot.date,
            appointmentSlot.status,
            appointmentSlot.startTime,
            appointmentSlot.endTime,
            appointmentSlot.appointmentId
        )!!
    }

    fun createPayment(appointmentSlot: AppointmentSlot): Int {
        val sql = """
            INSERT INTO appointment_slots
            ( psychologist_id, date,status, start_time, end_time,appointment_id)
            VALUES (?, ?, ?, ?, ?,?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            appointmentSlot.psychologyId,
            appointmentSlot.date,
            appointmentSlot.status,
            appointmentSlot.startTime,
            appointmentSlot.endTime,
            appointmentSlot.appointmentId
        )!!
    }
    fun getAppointmentSlotByDate(psychologyId :Int,date: Date): List<AppointmentSlot> {
        val sql = """
            SELECT *
            FROM appointment_slots
            WHERE date = ? and psychologist_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                AppointmentSlot(
                    id = rs.getInt("id"),
                    psychologyId = rs.getInt("psychologist_id"),
                    date = rs.getDate("date"),
                    startTime = rs.getString("start_time"),
                    endTime = rs.getString("end_time"),
                    status = rs.getString("status"),
                )
            },
            date,
            psychologyId
        )
    }

    fun getPsychologyAvailabilityByDay(psychologyid: Int,dayOfWeek: Int): List<PsychologyAvailability>? {
        val sql = """
            SELECT *
            FROM psychologist_availability
            WHERE psychologist_id = ? and day_of_week = ? 
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
            psychologyid,
            dayOfWeek
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
            psychologyid,
        )
    }


}