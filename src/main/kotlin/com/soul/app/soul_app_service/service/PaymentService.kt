package com.soul.app.soul_app_service.service

import com.midtrans.httpclient.error.MidtransError
import com.midtrans.service.MidtransSnapApi
import com.soul.app.soul_app_service.model.Payment
import com.soul.app.soul_app_service.repository.AppointmentRepository
import com.soul.app.soul_app_service.repository.PaymentRepository
import com.soul.app.soul_app_service.repository.PsychologyRepository
import com.soul.app.soul_app_service.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val appointmentRepository: AppointmentRepository,
    private val psychologyRepository: PsychologyRepository,
    private val snapApi: MidtransSnapApi,
    private val userRepository: UserRepository
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

        // 1️⃣ test payment (status CREATED)
        val paymentId = paymentRepository.createPayment(
            Payment(
                id = -99,
                appointmentId = appointmentId,
                payerUserId = userId,
                price = price,
                currency = "IDR",
                status = "CREATED"
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
            )
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

            // 3️⃣ Mark as FAILED if Midtrans error
            paymentRepository.updatePaymentStatus(
                paymentId = paymentId,
                status = "FAILED",
                paymentMethod = null,
                midtransTransactionId = null,
                paidAt = null
            )

            throw RuntimeException("Midtrans error: ${e.message}")
        }
    }
}