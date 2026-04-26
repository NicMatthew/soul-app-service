package com.soul.app.soul_app_service.service

import com.soul.app.soul_app_service.dto.AppointmentStatus
import com.soul.app.soul_app_service.dto.NotificationType
import com.soul.app.soul_app_service.dto.PaymentStatus
import com.soul.app.soul_app_service.dto.TimeSlot
import com.soul.app.soul_app_service.dto.TimeSlotWithStatus
import com.soul.app.soul_app_service.dto.request.CreateAppointmentRequest
import com.soul.app.soul_app_service.dto.request.DayOffRequest
import com.soul.app.soul_app_service.dto.request.RatingAppointmentRequest
import com.soul.app.soul_app_service.dto.request.addAppointmentNotesRequest
import com.soul.app.soul_app_service.dto.response.GetPsychologAppointmentResponse
import com.soul.app.soul_app_service.dto.response.GetUserAppointmentDetailResponse
import com.soul.app.soul_app_service.dto.response.GetUserAppointmentResponse
import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.AppointmentSlot
import com.soul.app.soul_app_service.model.Notification
import com.soul.app.soul_app_service.repository.AppointmentRepository
import com.soul.app.soul_app_service.repository.PaymentRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.emptyList

@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val psychologyService: PsychologyService,
    private val paymentService: PaymentService,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
    private val psychologyRepository: PsychologyRepository,
    private val notificationService: NotificationService,
) {
    fun createAppointment(
        userId: Int,
        request: CreateAppointmentRequest
    ): Appointment {
        updateAppointmentStatusByUserId(userId)
        request.psychologyId = psychologyRepository.getPsychologyProfileIdFromUserId(request.psychologyId)!!

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
        val psychologistUserId = appointmentRepository.getPsychologistUserIdByAppointmentId(appointmentId)!!
        val psychologistUser = userRepository.getUserById(psychologistUserId)!!
        notificationService.sendNotification(userId, Notification(
            userId = userId,
            type = NotificationType.PAYMENT_REMINDER.name  ,
            title = "Pengingat Pembayaran",
            description = " Pembayaran untuk Jadwal bersama ${psychologistUser.name} di ${request.date}",
            redirectUrl = "https://soulapp.my.id/payment/${appointmentId}",
        ))
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
            ) ?: return emptyList()

        if (availabilities.isEmpty()) return emptyList()

        val appointments =
            appointmentRepository.getAppointmentSlotByDate(
                psychologistId,
                date
            )!!.map {
                val start = LocalTime.parse(it.startTime, formatter)
                val end = LocalTime.parse(it.endTime, formatter)
                start to end
            }

        val result = mutableListOf<TimeSlot>()

        availabilities.forEach { availability ->

            var cursor = LocalTime.parse(availability.startTime, formatter)
            val availabilityEnd = LocalTime.parse(availability.endTime, formatter)

            while (cursor.plusHours(1) <= availabilityEnd) {

                val slotStart = cursor
                val slotEnd = cursor.plusHours(1)

                // 🔥 no parsing di dalam loop
                val isBooked = appointments.any { (apptStart, apptEnd) ->
                    slotStart < apptEnd && slotEnd > apptStart
                }

                if (!isBooked) {
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
    fun getMonthlyAvailabilityWithStatus(
        userId: Int
    ): Map<LocalDate, List<TimeSlotWithStatus>> {

        val log = LoggerFactory.getLogger("AvailabilityTrace")

        val psychology = psychologyService.getPsychologyDetailByUserId(userId)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val today = LocalDate.now()
        val now = LocalDateTime.now()

        val availabilities =
            appointmentRepository.getPsychologyAvailability(psychology!!.psychologyProfile.id)
                ?: return emptyMap()

        val endDate = today.plusDays(59)
        val allAppointments =
            appointmentRepository.getAppointmentsByDateRange(
                psychology.psychologyProfile.id,
                Date.valueOf(today),
                Date.valueOf(endDate)
            ) ?: emptyList()

        val appointmentMap = allAppointments.groupBy {
            it.date.toLocalDate()
        }.mapValues { entry ->
            entry.value.map {
                val start = LocalTime.parse(it.startTime, formatter)
                val end = LocalTime.parse(it.endTime, formatter)
                start to end
            }
        }

        val result = linkedMapOf<LocalDate, List<TimeSlotWithStatus>>()

        for (i in 0..59) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.value

            val dailyAvailabilities =
                availabilities.filter { it.dayOfWeek == dayOfWeek }

            if (dailyAvailabilities.isEmpty()) {
                result[date] = emptyList()
                continue
            }

            val dayAppointments = appointmentMap[date] ?: emptyList()

            val slots = mutableListOf<TimeSlotWithStatus>()

            dailyAvailabilities.forEach { availability ->

                var cursor = LocalTime.parse(availability.startTime, formatter)
                val availabilityEnd = LocalTime.parse(availability.endTime, formatter)

                while (cursor.plusHours(1) <= availabilityEnd) {

                    val slotStart = cursor
                    val slotEnd = cursor.plusHours(1)

                    val slotEndDT = LocalDateTime.of(date, slotEnd)
                    val isPast = slotEndDT.isBefore(now)

                    // 🔥 lebih ringan (no parsing di sini)
                    val isBooked = dayAppointments.any { (apptStart, apptEnd) ->
                        slotStart < apptEnd && slotEnd > apptStart
                    }

                    val status = when {
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

    fun addAppointmentNotes(request: addAppointmentNotesRequest, appointmentId: Int): Appointment {
        val appointment = appointmentRepository.getAppointmentById(appointmentId) ?: throw IllegalStateException("No appointment with id $appointmentId")

        if (appointment.status != AppointmentStatus.FINISHED.name) throw IllegalStateException("Appointment Still not finished yet with id $appointmentId")

        appointmentRepository.addAppointmentNotes(appointmentId,request)

        return appointmentRepository.getAppointmentById(appointmentId)!!

    }



    fun updateAppointmentStatusByUserId(userId: Int) {

        val appointments = appointmentRepository.getAppointmentsByUserId(userId, null, null, null)
        if (appointments.isNullOrEmpty()) return

        val now = LocalDateTime.now()

        appointments.forEach { appointment ->


            val localDate = appointment!!.date.toLocalDate()

            val startTime = LocalTime.parse(appointment.startTime)
            val endTime = LocalTime.parse(appointment.endTime)

            val startDateTime = LocalDateTime.of(localDate, startTime)

            val endDateTime = if (endTime.isBefore(startTime)) {
                LocalDateTime.of(localDate.plusDays(1), endTime)
            } else {
                LocalDateTime.of(localDate, endTime)
            }

            when {
                now.isAfter(startDateTime) && now.isBefore(endDateTime) -> {
                    if (appointment.status == AppointmentStatus.PAID.name) {
                        appointmentRepository.updateAppointmentStatus(
                            appointment.id,
                            AppointmentStatus.ONGOING.name
                        )
                    }

                }

                now.isAfter(endDateTime) -> {
                    if (appointment.status == AppointmentStatus.ONGOING.name || appointment.status ==AppointmentStatus.PAID.name) {
                        appointmentRepository.updateAppointmentStatus(
                            appointment.id,
                            AppointmentStatus.FINISHED.name
                        )
                    }
                }
            }
        }
    }
    fun getAllUserAppointments(userId: Int,status: String?,date: Date?,order: String?): List<GetUserAppointmentResponse>? {
        updateAppointmentStatusByUserId(userId)
        val response = mutableListOf<GetUserAppointmentResponse>()
        appointmentRepository.getAppointmentsByUserId(userId,status,date,order)?.forEach { appointment ->
            response.add(GetUserAppointmentResponse(
                appointment!!,
                userRepository.getUserByPsychologyId(appointment.psychologyId)!!.name,
                appointmentRepository.getRatingAppointmentByUserIdAndAppointmentId(userId,appointment.id)
            ))
        }
        return response
    }
    fun getAppointmentDetail(appointmentId: Int): GetUserAppointmentDetailResponse? {
        val appointment = appointmentRepository.getAppointmentById(appointmentId) ?: throw RuntimeException("Appointment not found")
        val payment = paymentRepository.getPaymentByAppointmentId(appointmentId) ?: throw RuntimeException("Payment not found")
        return GetUserAppointmentDetailResponse(
            appointment = appointment,
            payment = payment,
            userRepository.getUserByPsychologyId(appointment.psychologyId)!!.name,
            rating = appointmentRepository.getRatingAppointmentByAppointmentId(appointmentId)
        )
    }

    fun cancelAppointment(appointmentId: Int){
        appointmentRepository.updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED.name)
        paymentRepository.updatePaymentStatusByAppointmentId(appointmentId, PaymentStatus.FAILED.name)
        appointmentRepository.deleteAppointmentSlotbyAppointmentId(appointmentId)
    }
    fun getAllPsychologAppointments(userId: Int,status: String?, date: Date?,order:String?): List<GetPsychologAppointmentResponse>? {
        updateAppointmentStatusByUserId(userId)

        val appointments = appointmentRepository.getPsychologAppointmentsByPyschologyId(userId,status,date,order)
        val response = mutableListOf<GetPsychologAppointmentResponse>()
        appointments?.forEach { appointment ->
            val client = userRepository.getUserById(appointment.clientUserId)!!
            val name  = if (client.anonymous == true) "Anonymous" else client.name
            response.add(GetPsychologAppointmentResponse(
                appointment,
                client.anonymous,
                name

            ))
        }
        return response
    }

    fun getPatientsHistory(clientId: Int,psychologyId: Int,order: String?): List<GetPsychologAppointmentResponse>? {
        val appointments = appointmentRepository.getPatientsAppointmentsByPyschologyIdAndClientId(clientId,psychologyId,order)
        val response = mutableListOf<GetPsychologAppointmentResponse>()
        appointments?.forEach { appointment ->
            val client = userRepository.getUserById(appointment.clientUserId)!!
            val name  = if (client.anonymous == true) "Anonymous" else client.name
            response.add(GetPsychologAppointmentResponse(
                appointment,
                client.anonymous,
                name

            ))
        }
        return response
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
    fun addDayOff(userId: Int, request: DayOffRequest){
        appointmentRepository.addDayOff(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!,request)
    }

    fun deleteDayOff(dayOffId: Int){
        appointmentRepository.deleteDayOff(dayOffId)
    }
    fun getAllDayoff(userId: Int): List<AppointmentSlot>?{
        return appointmentRepository.getAllDayOff(psychologyRepository.getPsychologyProfileIdFromUserId(userId)!!)
    }
}