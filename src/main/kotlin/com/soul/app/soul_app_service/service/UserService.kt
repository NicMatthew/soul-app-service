package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.TimeSlot
import com.soul.app.soul_app_service.dto.TimeSlotWithStatus
import com.soul.app.soul_app_service.dto.request.CreateAppointmentRequest
import com.soul.app.soul_app_service.dto.request.UpdateProfileRequest
import com.soul.app.soul_app_service.dto.response.GetAppointmentDetailResponse
import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.AppointmentSlot
import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.repository.AppointmentRepository
import com.soul.app.soul_app_service.repository.PaymentRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class UserService(
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val psychologyService: PsychologyService,
    private val psychologyRepository: PsychologyRepository,
    private val paymentService: PaymentService,
    private val paymentRepository: PaymentRepository,

    ) {

    fun updateProfile(
        userId: Int,
        request: UpdateProfileRequest
    ): String {

        val user = userRepository.getUserById(userId)
            ?: throw IllegalArgumentException("User not found")

        val updatedUser = user.copy(
            username = request.username ?: user.username,
            phone = request.phone ?: user.phone,
            dob = request.dob ?: user.dob,
            gender = request.gender ?: user.gender,
            profile_picture = request.profilePicture ?: user.profile_picture,
            anonymous = request.anonymous ?: user.anonymous
        )

        userRepository.updateUser(updatedUser)

        return "user updated"
    }
    fun getUserByEmail(email: String): User {
        return (userRepository.getUserByEmail(email)?: userRepository.getUserByUsername(email))!!
    }
    fun getUserById(userId: Int): User? {
        return userRepository.getUserById(userId)
    }

    fun createAppointment(
        userId: Int,
        request: CreateAppointmentRequest
    ): Appointment {
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
            status = "WAITING_PAYMENT",
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
        //todo payment

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
    fun getAllAppointments(userId: Int): List<Appointment>? {
        return appointmentRepository.getAppointmentByUserId(userId)
    }

    fun getAppointmentDetail(appointmentId: Int): GetAppointmentDetailResponse? {
        val appointment = appointmentRepository.getAppointmentById(appointmentId) ?: throw RuntimeException("Appointment not found")
        val payment = paymentRepository.getPaymentByAppointmentId(appointmentId) ?: throw RuntimeException("Appointment not found")
        return GetAppointmentDetailResponse(
            appointment = appointment,
            payment = payment
        )
    }



}
