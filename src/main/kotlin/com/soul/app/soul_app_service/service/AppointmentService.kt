package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.AppointmentStatus
import com.soul.app.soul_app_service.dto.TimeSlot
import com.soul.app.soul_app_service.dto.TimeSlotWithStatus
import com.soul.app.soul_app_service.dto.request.CreateAppointmentRequest
import com.soul.app.soul_app_service.dto.request.RatingAppointmentRequest
import com.soul.app.soul_app_service.dto.request.addAppointmentNotesRequest
import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.AppointmentSlot
import com.soul.app.soul_app_service.repository.AppointmentRepository
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val psychologyService: PsychologyService,
    private val paymentService: PaymentService,
) {
    fun createAppointment(
        userId: Int,
        request: CreateAppointmentRequest
    ): Appointment {
        updateAppointmentStatusByUserId(userId)
        request.psychologyId = psychologyService.getPsychologyDetailByUserId(request.psychologyId)!!.psychologyProfile.id

        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val requestStart = LocalTime.parse(request.startTime, formatter)
        val requestEnd = LocalTime.parse(request.endTime, formatter)

        if (!requestStart.isBefore(requestEnd)) {
            throw IllegalArgumentException("Start time must be before end time")
        }

        val availableSlots =
            getAvailableSlots(request.psychologyId, request.date)

        val isValidSlot = availableSlots.any { slot ->
            val slotStart = LocalTime.parse(slot.startTime, formatter)
            val slotEnd = LocalTime.parse(slot.endTime, formatter)

            requestStart == slotStart && requestEnd == slotEnd
        }

        if (!isValidSlot) {
            throw IllegalStateException(
                "Requested time is not available"
            )
        }

        val appointmentId = appointmentRepository.createAppointment(
            userId = userId,
            request = request,
            status = AppointmentStatus.WAITING_PAYMENT.name,
        )
        appointmentRepository.createAppointmentSlot(
            AppointmentSlot(
                id = -99,
                psychologyId = request.psychologyId,
                date = request.date,
                startTime = request.startTime,
                endTime = request.endTime,
                status = "BOOKED",
                appointmentId = appointmentId,
            )
        )
        paymentService.createPayment(appointmentId,userId)
        return appointmentRepository.getAppointmentById(appointmentId)!!
    }
    fun getAvailableSlots(
        psychologistId: Int,
        date: Date
    ): List<TimeSlot> {


        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val localDate = date.toLocalDate()
        val dayOfWeek = localDate.dayOfWeek.value

        val availabilities =
            appointmentRepository.getPsychologyAvailabilityByDay(
                psychologistId,
                dayOfWeek
            )

        if (availabilities != null) {
            if (availabilities.isEmpty()) return emptyList()
        }

        val appointments =
            appointmentRepository.getAppointmentSlotByDate(
                psychologistId,
                date
            )

        val result = mutableListOf<TimeSlot>()

        availabilities?.forEach { availability ->

            var cursor = LocalTime.parse(availability.startTime, formatter)
            val availabilityEnd =
                LocalTime.parse(availability.endTime, formatter)

            while (cursor.plusHours(1) <= availabilityEnd) {

                val slotStart = cursor
                val slotEnd = cursor.plusHours(1)

                val conflict = appointments.any { appt ->
                    val apptStart =
                        LocalTime.parse(appt.startTime, formatter)
                    val apptEnd =
                        LocalTime.parse(appt.endTime, formatter)

                    // STRICT overlap
                    slotStart < apptEnd && slotEnd > apptStart
                }

                if (!conflict) {
                    result.add(
                        TimeSlot(
                            startTime = slotStart.format(formatter),
                            endTime = slotEnd.format(formatter)
                        )
                    )
                }

                cursor = cursor.plusHours(1)
            }
        }

        return result
    }
    fun getWeeklyAvailabilityWithStatus(
        userId: Int
    ): Map<LocalDate, List<TimeSlotWithStatus>> {
        val psychology = psychologyService.getPsychologyDetailByUserId(userId)

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val today = LocalDate.now()
        val nowTime = LocalTime.now()

        val availabilities =
            appointmentRepository.getPsychologyAvailability(psychology!!.psychologyProfile.id)
                ?: return emptyMap()

        val result = linkedMapOf<LocalDate, List<TimeSlotWithStatus>>()

        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.value

            val dailyAvailabilities =
                availabilities.filter { it.dayOfWeek == dayOfWeek }

            if (dailyAvailabilities.isEmpty()) {
                result[date] = emptyList()
                continue
            }

            val appointments =
                appointmentRepository.getAppointmentSlotByDate(
                    psychology.psychologyProfile.id,
                    Date.valueOf(date)
                )

            val slots = mutableListOf<TimeSlotWithStatus>()

            dailyAvailabilities.forEach { availability ->

                var cursor =
                    LocalTime.parse(availability.startTime, formatter)

                val availabilityEnd =
                    LocalTime.parse(availability.endTime, formatter)

                while (cursor.plusHours(1) <= availabilityEnd) {

                    val slotStart = cursor
                    val slotEnd = cursor.plusHours(1)

                    val isPast =
                        date == today && slotEnd <= nowTime

                    val isBooked =
                        appointments.any { appt ->
                            val apptStart =
                                LocalTime.parse(appt.startTime, formatter)
                            val apptEnd =
                                LocalTime.parse(appt.endTime, formatter)

                            slotStart < apptEnd && slotEnd > apptStart
                        }

                    val status =
                        when {
                            isBooked -> "BOOKED"
                            isPast -> "UNAVAILABLE"
                            else -> "AVAILABLE"
                        }

                    slots.add(
                        TimeSlotWithStatus(
                            startTime = slotStart.format(formatter),
                            endTime = slotEnd.format(formatter),
                            status = status
                        )
                    )

                    cursor = cursor.plusHours(1)
                }
            }

            result[date] = slots
        }

        return result
    }

    fun addAppointmentNotes(request: addAppointmentNotesRequest, appointmentId: Int): String {
        val appointment = appointmentRepository.getAppointmentById(appointmentId) ?: throw IllegalStateException("No appointment with id $appointmentId")

        if (appointment.status != AppointmentStatus.FINISHED.name) throw IllegalStateException("Appointment Still not finished yet with id $appointmentId")

        appointmentRepository.addAppointmentNotes(appointmentId,request)

        return "Success add notes"

    }



    fun updateAppointmentStatusByUserId(userId: Int) {

        val appointments = appointmentRepository.getAppointmentsByUserId(userId)
            ?: throw IllegalStateException("No appointment with user id $userId")

        val now = LocalDateTime.now()

        appointments.forEach { appointment ->

            val date = LocalDate.parse(appointment.scheduledAt)
            val startTime = LocalTime.parse(appointment.startTime)
            val endTime = LocalTime.parse(appointment.endTime)

            val startDateTime = LocalDateTime.of(date, startTime)
            val endDateTime = LocalDateTime.of(date, endTime)

            when {

                now.isAfter(startDateTime) && now.isBefore(endDateTime) -> {
                    appointmentRepository.updateAppointmentStatus(
                        appointment.id,
                        AppointmentStatus.ONGOING.name
                    )
                }

                now.isAfter(endDateTime) -> {
                    appointmentRepository.updateAppointmentStatus(
                        appointment.id,
                        AppointmentStatus.FINISHED.name
                    )
                }
            }
        }
    }
    fun getAllUserAppointments(userId: Int): List<Appointment>? {
        updateAppointmentStatusByUserId(userId)
        return appointmentRepository.getAppointmentsByUserId(userId)
    }

    fun rateAppointment(userId: Int,request: RatingAppointmentRequest, appointmentId: Int) : String{
        val appointment = appointmentRepository.getAppointmentById(appointmentId) ?: throw IllegalStateException("No appointment with id $appointmentId")

        if (appointment.status == AppointmentStatus.FINISHED.name) {
            appointmentRepository.addAppointmentRating(userId, request, appointmentId,appointment.psychologyId)
        }
        else {
            throw IllegalStateException("Appointment not eligible for rating with id :  $appointmentId and status : ${appointment.status}")
        }

        return "Rating Added"
    }
}