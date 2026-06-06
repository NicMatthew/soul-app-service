package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.model.Appointment
import com.soul.app.soul_app_service.model.Conversation
import com.soul.app.soul_app_service.model.Message
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import kotlin.collections.firstOrNull

@Repository
class ChatRepository(
    private val jdbcTemplate: JdbcTemplate
) {


    fun getConversationById(conversationId: Int): Conversation? {
        val sql = """
            SELECT *
            FROM conversations
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                Conversation(
                    id = rs.getInt("id"),
                    appointmentId = rs.getInt("appointment_id"),
                    createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                    lastMessageAt = rs.getTimestamp("last_message_at")?.toLocalDateTime()
                )
            },
            conversationId
        ).firstOrNull()
    }

    fun updateLastMessageAt(conversationId: Int) {
        val sql = """
            UPDATE conversations
            SET last_message_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(sql, conversationId)
    }

    fun getConversationParticipants(conversationId: Int): List<Int>? {
        val sql = """
            SELECT 
                a.client_user_id AS client_user_id,
                pp.user_id AS psychologist_user_id
            FROM conversations c
            JOIN appointments a ON a.id = c.appointment_id
            JOIN psychologist_profile pp ON pp.id = a.psychologist_id
            WHERE c.id = ?
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                listOf(
                    rs.getInt("client_user_id"),
                    rs.getInt("psychologist_user_id")
                )
            },
            conversationId
        ).firstOrNull()
    }

    fun getAppointmentByConversationId(conversationId: Int): Appointment? {
        val sql = StringBuilder( """
        SELECT *
        FROM appointments a left join conversations c on c.appointment_id = a.id
        WHERE c.id = ?
        """)


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
            conversationId
        ).firstOrNull()
    }

    fun createMessage(message: Message): Int {
        val sql = """
            INSERT INTO messages
            (conversation_id, sender_user_id, message_text, sent_at)
            VALUES (?, ?, ?, ?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            message.conversationId,
            message.senderUserId,
            message.messageText,
            message.sentAt
        )
    }
    fun createConversation(appointmentId : Int): Int {
        val sql = """
            INSERT INTO conversations
            (appointment_id, created_at)
            VALUES (?, NOW())
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            appointmentId
        )
    }

    fun getMessagesByConversationId(conversationId: Int): List<Message> {
        val sql = """
            SELECT *
            FROM messages
            WHERE conversation_id = ?
            ORDER BY sent_at ASC
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                Message(
                    id = rs.getInt("id"),
                    conversationId = rs.getInt("conversation_id"),
                    senderUserId = rs.getInt("sender_user_id"),
                    messageText = rs.getString("message_text"),
                    sentAt = rs.getTimestamp("sent_at").toLocalDateTime()
                )
            },
            conversationId
        )
    }


    fun markMessagesAsRead(conversationId: Int, readerUserId: Int): Int {
        val sql = """
        UPDATE messages 
        SET is_read = TRUE, read_at = NOW()
        WHERE conversation_id = ?
          AND sender_user_id != ?
          AND is_read = FALSE
    """
        return jdbcTemplate.update(sql, conversationId, readerUserId)
    }

    fun getUnreadCount(conversationId: Int, userId: Int): Int {
        val sql = """
        SELECT COUNT(*) FROM messages
        WHERE conversation_id = ?
          AND sender_user_id != ?
          AND is_read = FALSE
    """
        return jdbcTemplate.queryForObject(sql, Int::class.java, conversationId, userId)
    }
    fun getConversationsByUserId(userId: Int): List<Conversation> {
        val sql = """
        SELECT c.*
        FROM conversations c
        JOIN appointments a ON a.id = c.appointment_id
        JOIN psychologist_profile pp ON pp.id = a.psychologist_id
        WHERE a.client_user_id = ? OR pp.user_id = ?
        ORDER BY c.last_message_at DESC NULLS LAST
    """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                Conversation(
                    id = rs.getInt("id"),
                    appointmentId = rs.getInt("appointment_id"),
                    createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                    lastMessageAt = rs.getTimestamp("last_message_at")?.toLocalDateTime()
                )
            },
            userId, userId
        )
    }

    fun getLastMessage(conversationId: Int): Message? {
        val sql = """
        SELECT * FROM messages
        WHERE conversation_id = ?
        ORDER BY sent_at DESC
        LIMIT 1
    """.trimIndent()

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                Message(
                    id = rs.getInt("id"),
                    conversationId = rs.getInt("conversation_id"),
                    senderUserId = rs.getInt("sender_user_id"),
                    messageText = rs.getString("message_text"),
                    sentAt = rs.getTimestamp("sent_at").toLocalDateTime()
                )
            },
            conversationId
        ).firstOrNull()
    }

    fun getOtherUserId(conversationId: Int, userId: Int): Int? {
        val sql = """
        SELECT 
            CASE 
                WHEN a.client_user_id = ? THEN pp.user_id
                ELSE a.client_user_id
            END AS other_user_id
        FROM conversations c
        JOIN appointments a ON a.id = c.appointment_id
        JOIN psychologist_profile pp ON pp.id = a.psychologist_id
        WHERE c.id = ?
    """.trimIndent()

        return jdbcTemplate.queryForObject(sql, Int::class.java, userId, conversationId)
    }
}
