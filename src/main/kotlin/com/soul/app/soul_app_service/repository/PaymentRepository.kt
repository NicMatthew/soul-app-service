package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.model.Payment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.collections.firstOrNull

@Repository
class PaymentRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun createPayment(payment: Payment): Int {
        val sql = """
            INSERT INTO payments
            (appointment_id, payer_user_id, price, currency, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, NOW(), NOW())
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            payment.appointmentId,
            payment.payerUserId,
            payment.price,
            payment.currency,
            payment.status
        )!!
    }

    fun updateSnapToken(paymentId: Int, snapToken: String) {
        val sql = """
            UPDATE payments
            SET snap_token = ?, status = 'PENDING', updated_at = NOW()
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(sql, snapToken, paymentId)
    }

    fun updatePaymentStatus(
        paymentId: Int,
        status: String,
        paymentMethod: String?,
        midtransTransactionId: String?,
        paidAt: LocalDateTime?
    ) {
        val sql = """
            UPDATE payments
            SET status = ?,
                payment_method = ?,
                midtrans_transaction_id = ?,
                paid_at = ?,
                updated_at = NOW()
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            status,
            paymentMethod,
            midtransTransactionId,
            paidAt?.let { Timestamp.valueOf(it) },
            paymentId
        )
    }

    fun updatePaymentStatusByAppointmentId(
        appointmentId: Int,
        status: String,
    ) {
        val sql = """
            UPDATE payments
            SET status = ?,
                updated_at = NOW()
            WHERE appointment_id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            status,
            appointmentId
        )
    }

    fun existsByAppointmentId(appointmentId: Int): Boolean {
        val sql = """
            SELECT COUNT(*) FROM payments WHERE appointment_id = ?
        """.trimIndent()

        val count = jdbcTemplate.queryForObject(sql, Int::class.java, appointmentId)
        return (count ?: 0) > 0
    }

    fun getPaymentById(paymentId: Int): Payment? {
        val sql = """
            SELECT * FROM payments WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            paymentRowMapper(),
            paymentId
        ).firstOrNull()
    }

    private fun paymentRowMapper() = RowMapper { rs, _ ->
        Payment(
            id = rs.getInt("id"),
            appointmentId = rs.getInt("appointment_id"),
            payerUserId = rs.getInt("payer_user_id"),
            price = rs.getInt("price"),
            currency = rs.getString("currency"),
            status = rs.getString("status"),
            paymentMethod = rs.getString("payment_method"),
            snapToken = rs.getString("snap_token"),
            midtransTransactionId = rs.getString("midtrans_transaction_id"),
            paidAt = rs.getTimestamp("paid_at")?.toString(),
            createdAt = rs.getTimestamp("created_at")?.toString(),
            updatedAt = rs.getTimestamp("updated_at")?.toString()
        )
    }
    fun getPaymentByAppointmentId(appointmentId: Int): Payment? {
        val sql = """
            SELECT * FROM payments WHERE appointment_id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            paymentRowMapper(),
            appointmentId
        ).firstOrNull()
    }
}