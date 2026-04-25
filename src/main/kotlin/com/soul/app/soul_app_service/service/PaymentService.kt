package com.soul.app.soul_app_service.service

import com.midtrans.httpclient.error.MidtransError
import com.midtrans.service.MidtransSnapApi
import com.soul.app.soul_app_service.dto.AppointmentStatus
import com.soul.app.soul_app_service.dto.NotificationType
import com.soul.app.soul_app_service.dto.PaymentStatus
import com.soul.app.soul_app_service.model.Notification
import com.soul.app.soul_app_service.model.Payment
import com.soul.app.soul_app_service.repository.AppointmentRepository
import com.soul.app.soul_app_service.repository.PaymentRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val appointmentRepository: AppointmentRepository,
    private val psychologyRepository: PsychologyRepository,
    private val snapApi: MidtransSnapApi,
    private val userRepository: UserRepository,
    @Value("\${midtrans.server-key}")
    private val serverKey: String,
    private val notificationService: NotificationService
) {

    @Transactional
    fun createPayment(appointmentId: Int, userId: Int): String {

        val appointment = appointmentRepository.getAppointmentById(appointmentId)
            ?: throw IllegalArgumentException("No appointment with ID $appointmentId")

        if (appointment.status != "WAITING_PAYMENT") {
            throw IllegalStateException("Appointment not eligible for payment")
        }

        if (paymentRepository.existsByAppointmentId(appointmentId)) {
            throw IllegalStateException("Payment already exists")
        }

        val price = psychologyRepository
            .getPsychologyProfilebyProfileId(appointment.psychologyId)
            ?.pricePerSession

        if (price == null || price <= 0) {
            throw IllegalStateException("Invalid price")
        }

        val paymentId = paymentRepository.createPayment(
            Payment(
                id = -99,
                appointmentId = appointmentId,
                payerUserId = userId,
                price = price,
                currency = "IDR",
                status = PaymentStatus.CREATED.name
            )
        )

        val orderId = paymentId.toString()

        val client = userRepository.getUserById(appointment.clientUserId)
            ?: throw IllegalArgumentException("No user with ID ${appointment.clientUserId}")

        val params = hashMapOf<String, Any>(
            "transaction_details" to hashMapOf(
                "order_id" to orderId,
                "gross_amount" to price
            ),
            "customer_details" to hashMapOf(
                "first_name" to client.name,
                "email" to client.email
            ),
        )

        try {
            val snapToken = snapApi.createTransactionToken(params)

            // 2️⃣ Update snap token + set PENDING
            paymentRepository.updateSnapToken(
                paymentId = paymentId,
                snapToken = snapToken
            )

            return snapToken

        } catch (e: MidtransError) {

            paymentRepository.updatePaymentStatus(
                paymentId = paymentId,
                status = PaymentStatus.FAILED.name,
                paymentMethod = null,
                midtransTransactionId = null,
                paidAt = null
            )

            throw RuntimeException("Midtrans error: ${e.message}")
        }
    }
    private fun generateSignature(
        orderId: String,
        statusCode: String,
        grossAmount: String
    ): String {

        val raw = orderId + statusCode + grossAmount + serverKey

        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(raw.toByteArray())

        return digest.joinToString("") { "%02x".format(it) }
    }
    @Transactional
    fun handleMidtransNotification(payload: Map<String, Any>) {

        val orderId = payload["order_id"] as String
        val transactionStatus = payload["transaction_status"] as String
        val fraudStatus = payload["fraud_status"] as? String
        val signatureKey = payload["signature_key"] as String
        val grossAmount = payload["gross_amount"] as String

        val expectedSignature = generateSignature(
            orderId,
            payload["status_code"] as String,
            grossAmount
        )

        if (signatureKey != expectedSignature) {
            throw IllegalArgumentException("Invalid signature")
        }

        val paymentId = orderId.removePrefix("PAY-").toInt()

        val payment = paymentRepository.getPaymentById(paymentId)
            ?: throw IllegalArgumentException("Payment not found")

        if (payment.status == PaymentStatus.PAID.name) {
            return
        }

        when (transactionStatus) {
            "capture", "settlement" -> {
                if (fraudStatus == null || fraudStatus == "accept") {

                    paymentRepository.updatePaymentStatus(
                        paymentId = paymentId,
                        status = PaymentStatus.PAID.name,
                        paymentMethod = payload["payment_type"] as? String,
                        midtransTransactionId = payload["transaction_id"] as? String,
                        paidAt = LocalDateTime.now()
                    )

                    appointmentRepository.updateAppointmentStatus(
                        payment.appointmentId,
                        AppointmentStatus.PAID.name,
                    )
                    val appointment = appointmentRepository.getAppointmentSlotByAppointmentId(payment.appointmentId)!!


                    val clientId = appointmentRepository.getClientUserIdByAppointmentId(payment.appointmentId)!!
                    val clientUser = userRepository.getUserById(clientId)!!
                    val psychologistUserId = appointmentRepository.getPsychologistUserIdByAppointmentId(payment.appointmentId)!!
                    val psychologistUser = userRepository.getUserById(psychologistUserId)!!

                    notificationService.sendNotification(clientId, Notification(
                        userId = clientId,
                        type = NotificationType.APPOINTMENT_REMINDER.name  ,
                        title = "Pengingat Jadwal",
                        description = "Jadwal bersama ${psychologistUser.name} di ${appointment.startTime} pada ${appointment.date}",
                        redirectUrl = "https://soulapp.my.id/consultation/${appointment.id}",
                    ))

                    notificationService.sendNotification(clientId, Notification(
                        userId = clientId,
                        type = NotificationType.APPOINTMENT_REMINDER.name  ,
                        title = "Pengingat Jadwal",
                        description = "Jadwal bersama ${clientUser.name} di ${appointment.startTime} pada ${appointment.date}",
                        redirectUrl = "https://soulapp.my.id/consultation/${appointment.id}",
                    ))

                }
            }

            "pending" -> {
                paymentRepository.updatePaymentStatus(
                    paymentId = paymentId,
                    status = PaymentStatus.PENDING.name,
                    paymentMethod = null,
                    midtransTransactionId = null,
                    paidAt = null
                )
            }

            "deny", "cancel", "expire" -> {
                paymentRepository.updatePaymentStatus(
                    paymentId = paymentId,
                    status = PaymentStatus.FAILED.name,
                    paymentMethod = null,
                    midtransTransactionId = null,
                    paidAt = null
                )
            }
        }
    }
}