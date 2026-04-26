package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.dto.AppointmentStatus
import com.soul.app.soul_app_service.dto.request.CreateAppointmentRequest
import com.soul.app.soul_app_service.dto.request.DayOffRequest
import com.soul.app.soul_app_service.dto.request.RatingAppointmentRequest
import com.soul.app.soul_app_service.dto.request.addAppointmentNotesRequest
import com.soul.app.soul_app_service.dto.response.RatingResponse
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
            (client_user_id, psychologist_id, status, start_time, end_time,date,scheduled_at)
            VALUES (?, ?, ?, ?, ?,?,NOW())
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            userId,
            request.psychologyId,
            status,
            request.startTime,
            request.endTime,
            request.date
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
                    medicalNotes = rs.getString("medical_notes"),
                    finalDiagnose = rs.getString("final_diagnose"),
                    date = rs.getDate("date"),
                )
            },
            appointmentId
        ).firstOrNull()
    }
    fun getAppointmentsByUserId(userId: Int,status: String?,date: Date?,order: String?): List<Appointment?>? {
        val sql = StringBuilder("""
            SELECT *
            FROM appointments a
            WHERE a.client_user_id = ?
    """)
        val params = mutableListOf<Any>(userId)

        if (!status.isNullOrBlank()) {
            if (status == AppointmentStatus.NOT_STARTED.name){
                sql.append(" AND a.status IN (?, ?, ?)")
                params.add(AppointmentStatus.WAITING_PAYMENT.name)
                params.add(AppointmentStatus.PAID.name)
                params.add(AppointmentStatus.CREATED.name)
            }else {
                sql.append(" AND a.status = ?")
                params.add(status)

            }
        }
        // 🔍 FILTER DATE
        if (date != null) {
            sql.append(" AND DATE(a.scheduled_at) = ?")
            params.add(date)
        }

        // 🔃 ORDERING
        if (!order.isNullOrBlank()) {
            when (order.lowercase()) {
                "asc" -> sql.append(" ORDER BY a.scheduled_at ASC")
                "desc" -> sql.append(" ORDER BY a.scheduled_at DESC")
            }
        } else {
            sql.append(" ORDER BY a.scheduled_at DESC")
        }

        return jdbcTemplate.query(
            sql.toString(),
            params.toTypedArray())
            { rs, _ ->
                Appointment(
                    id = rs.getInt("id"),
                    clientUserId = rs.getInt("client_user_id"),
                    psychologyId = rs.getInt("psychologist_id"),
                    scheduledAt = rs.getString("scheduled_at"),
                    startTime = rs.getString("start_time"),
                    endTime = rs.getString("end_time"),
                    status = rs.getString("status"),
                    medicalNotes = rs.getString("medical_notes"),
                    finalDiagnose = rs.getString("final_diagnose"),
                    date = rs.getDate("date"),

                    )
            }

    }
    fun getPsychologAppointmentsByPyschologyId(userId: Int, status: String?, date: Date?, order:String?): List<Appointment>? {
            val sql = StringBuilder("""
            SELECT *
            FROM appointments a left join psychologist_profile p on p.id = a.psychologist_id
            WHERE p.user_id = ?
        """)

            val params = mutableListOf<Any>(userId)

            if (!status.isNullOrBlank()) {
                if (status == AppointmentStatus.NOT_STARTED.name){
                    sql.append(" AND a.status IN (?, ?, ?)")
                    params.add(AppointmentStatus.WAITING_PAYMENT.name)
                    params.add(AppointmentStatus.PAID.name)
                    params.add(AppointmentStatus.CREATED.name)
                }else {
                    sql.append(" AND a.status = ?")
                    params.add(status)

                }
            }
            // 🔍 FILTER DATE
            if (date != null) {
                sql.append(" AND DATE(a.scheduled_at) = ?")
                params.add(date)
            }

            // 🔃 ORDERING
            if (!order.isNullOrBlank()) {
                when (order.lowercase()) {
                    "asc" -> sql.append(" ORDER BY a.scheduled_at ASC")
                    "desc" -> sql.append(" ORDER BY a.scheduled_at DESC")
                }
            } else {
                sql.append(" ORDER BY a.scheduled_at DESC")
            }

            return jdbcTemplate.query(
                sql.toString(),
                { rs, _ ->
                    Appointment(
                        id = rs.getInt("id"),
                        clientUserId = rs.getInt("client_user_id"),
                        psychologyId = rs.getInt("psychologist_id"),
                        scheduledAt = rs.getString("scheduled_at"),
                        startTime = rs.getString("start_time"),
                        endTime = rs.getString("end_time"),
                        status = rs.getString("status"),
                        medicalNotes = rs.getString("medical_notes"),
                        finalDiagnose = rs.getString("final_diagnose"),
                        date = rs.getDate("date"),

                        )
                },
                *params.toTypedArray()
            )
    }

    fun getPatientsAppointmentsByPyschologyIdAndClientId(clientId: Int,psychologyid: Int,order: String?): List<Appointment>? {
        val sql = StringBuilder( """
        SELECT *
        FROM appointments a left join psychologist_profile p on p.id = a.psychologist_id
        WHERE a.client_user_id = ? AND p.user_id = ?
        """)

        if (!order.isNullOrBlank()) {
            when (order.lowercase()) {
                "asc" -> sql.append(" ORDER BY a.scheduled_at ASC")
                "desc" -> sql.append(" ORDER BY a.scheduled_at DESC")
            }
        } else {
            sql.append(" ORDER BY a.scheduled_at DESC")
        }

        return jdbcTemplate.query(
            sql.toString(),
            { rs, _ ->
                Appointment(
                    id = rs.getInt("id"),
                    clientUserId = rs.getInt("client_user_id"),
                    psychologyId = rs.getInt("psychologist_id"),
                    scheduledAt = rs.getString("scheduled_at"),
                    startTime = rs.getString("start_time"),
                    endTime = rs.getString("end_time"),
                    status = rs.getString("status"),
                    medicalNotes = rs.getString("medical_notes"),
                    finalDiagnose = rs.getString("final_diagnose"),
                    date = rs.getDate("date"),
                    )
            },
            clientId,
            psychologyid
        )
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


    fun getAppointmentSlotByDate(psychologyId :Int,date: Date): List<AppointmentSlot>? {
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
                    createdAt = rs.getTimestamp("created_at"),
                    )
            },
            date,
            psychologyId
        )
    }
    fun getAppointmentsByDateRange(
        psychologyId: Int,
        startDate: Date,
        endDate: Date
    ): List<AppointmentSlot>? {

        val sql = """
        SELECT *
        FROM appointment_slots
        WHERE psychologist_id = ?
        AND date BETWEEN ? AND ?
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
                    createdAt = rs.getTimestamp("created_at"),
                )
            },
            psychologyId,
            startDate,
            endDate
        )
    }

    fun getAppointmentSlotByAppointmentId(appointmentId: Int): AppointmentSlot? {
        val sql = """
            SELECT *
            FROM appointment_slots
            WHERE appointment_id = ?
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
                    createdAt = rs.getTimestamp("created_at"),

                    )
            },
            appointmentId,
        ).firstOrNull()
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

    fun updateAppointmentStatus(appointmentId: Int, status: String): Int {
        val sql = """
        UPDATE appointments
        SET status = ?
        WHERE id = ?
    """.trimIndent()

        return jdbcTemplate.update(
            sql,
            status,
            appointmentId
        )
    }

    fun addAppointmentNotes (appointmentId: Int, request: addAppointmentNotesRequest): Int {
        val sql = """
        UPDATE appointments
        SET medical_notes = ?, final_diagnose = ?
        WHERE id = ?
    """.trimIndent()

        return jdbcTemplate.update(
            sql,
            request.medicalNotes,
            request.finalDiagnose,
            appointmentId
        )
    }

    fun deleteAppointmentSlotbyAppointmentId(appointmentId: Int): Int {
        val sql = """
            DELETE FROM appointment_slots where appointment_id = ?
        """.trimIndent()
        return jdbcTemplate.update(
            sql,
            appointmentId,
        )
    }

    fun addAppointmentRating(userId: Int,request: RatingAppointmentRequest,appointmentId: Int,psychologyId: Int): Int{
        val sql = """
            INSERT INTO rating (
                psychologist_id,
                client_user_id,
                appointment_id,
                rate,
                description)
            VALUES (
                ?, ?, ?, ?, ?
            ) RETURNING id;
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            psychologyId,
            userId,
            appointmentId,
            request.rate,
            request.description
        )
    }

    fun getRatingAppointmentByUserIdAndAppointmentId(userId: Int, appointmentId: Int): Int?{
        val sql = """
            SELECT * 
            FROM rating 
            WHERE client_user_id = ? AND appointment_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
               rs.getInt("rate")
            },
            userId,
            appointmentId
        ).firstOrNull()
    }

    fun getClientUserIdByAppointmentId(appointmentId: Int): Int?{
        val sql = """
            select client_user_id from appointments where id = ?
        """.trimIndent()
        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                rs.getInt("client_user_id")
            },
            appointmentId,
        ).firstOrNull()
    }

    fun getPsychologistUserIdByAppointmentId(appointmentId: Int): Int?{
        val sql = """
            select p.user_id from appointments a left join psychologist_profile p on p.id = a.psychologist_id where a.id = ?
        """.trimIndent()
        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                rs.getInt("user_id")
            },
            appointmentId,
        ).firstOrNull()
    }
    fun getRatingAppointmentByAppointmentId(appointmentId: Int): RatingResponse?{
        val sql = """
            SELECT *
            FROM rating r left join users u on u.id = r.client_user_id 
            WHERE appointment_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                RatingResponse(
                    rate = rs.getInt("rate"),
                    description = rs.getString("description"),
                    userProfilePicture = rs.getString("profile_picture"),
                    userName = rs.getString("name"),
                    createdAt = rs.getTimestamp("created_at"),
                )
            },
            appointmentId
        ).firstOrNull()
    }
    fun addDayOff(profileId: Int,request: DayOffRequest): Int {
        val sql = """
            INSERT INTO appointment_slots
            ( psychologist_id, date,status, start_time, end_time)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            profileId,
            request.date,
            "DAY_OFF",
            request.startTime,
            request.endTime,
        )!!
    }


    fun deleteDayOff(dayOffId: Int): Int {
        val sql = """
            DELETE FROM appointment_slots WHERE id = ? AND status = ?
        """.trimIndent()

        return jdbcTemplate.update(
            sql,
            dayOffId,
            "DAY_OFF",
        )
    }

    fun getAllDayOff(profileId: Int): List<AppointmentSlot>? {
        val sql = """
        SELECT * 
        FROM appointment_slots 
        WHERE psychologist_id = ?
          AND status = ?
          AND date >= CURRENT_DATE
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
                    createdAt = rs.getTimestamp("created_at"),
                )
            },
            profileId,
            "DAY_OFF"
        )
    }




}