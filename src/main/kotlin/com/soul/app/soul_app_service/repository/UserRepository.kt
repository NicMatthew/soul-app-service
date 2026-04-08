package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.dto.request.RatingAppRequest
import com.soul.app.soul_app_service.model.RatingApp
import com.soul.app.soul_app_service.model.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun saveUser(user: User): Int {
        val sql = """
            INSERT INTO users
            (email, name, password_hash, username, phone, role, profile_picture, dob, gender, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            user.email,
            user.name,
            user.password_hash,
            user.username,
            user.phone,
            user.role,
            user.profile_picture,
            user.dob,
            user.gender
        )
    }

    fun submitRatingApp(userId: Int,request: RatingAppRequest): Int {
        val sql = """
            INSERT INTO rating_app
            (rate,description,client_user_id)
            VALUES (?, ?, ?)
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            request.rate,
            request.description,
            userId
        )
    }
    fun getAllRatingApp(): List<RatingApp> {
        val sql = "SELECT * FROM rating_app"

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ ->
                RatingApp(
                    userId = rs.getInt("client_user_id"),
                    rate = rs.getInt("rate"),
                    description = rs.getString("description"),
                )
            },
        )
    }


    fun getUserByEmail(email: String): User? {
        val sql = "SELECT * FROM users WHERE email = ?"

        return jdbcTemplate.query(
            sql,
            userRowMapper(),
            email
        ).firstOrNull()
    }

    fun getUserByUsername(username: String): User? {
        val sql = "SELECT * FROM users WHERE username = ?"

        return jdbcTemplate.query(
            sql,
            userRowMapper(),
            username
        ).firstOrNull()
    }

    fun getUserById(id: Int): User? {
        val sql = "SELECT * FROM users WHERE id = ?"

        return jdbcTemplate.query(
            sql,
            userRowMapper(),
            id
        ).firstOrNull()
    }

    fun getAllPsychologyUser(search: String?): List<User> {

        val sql = StringBuilder("SELECT * FROM users WHERE role = ?")
        val params = mutableListOf<Any>("psycholog")

        if (!search.isNullOrBlank()) {
            sql.append(" AND name ILIKE ?")
            params.add("%$search%")
        }

        return jdbcTemplate.query(
            sql.toString(),
            userRowMapper(),
            *params.toTypedArray()
        )
    }

    fun updateUser(user: User): Int {
        val sql = """
            UPDATE users
            SET username = ?,
                phone = ?,
                dob = ?,
                gender = ?,
                profile_picture = ?,
                anonymous = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING id
        """.trimIndent()

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            user.username,
            user.phone,
            user.dob,
            user.gender,
            user.profile_picture,
            user.anonymous,
            user.id
        )!!
    }

    fun deleteUser(id: Int): Int {
        val sql = "DELETE FROM users WHERE id = ? RETURNING id"

        return jdbcTemplate.queryForObject(
            sql,
            Int::class.java,
            id
        )!!
    }

    private fun userRowMapper(): RowMapper<User> =
        RowMapper { rs, _ ->
            User(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                password_hash = rs.getString("password_hash"),
                username = rs.getString("username"),
                phone = rs.getString("phone"),
                role = rs.getString("role"),
                profile_picture = rs.getString("profile_picture"),
                dob = rs.getDate("dob"),
                gender = rs.getString("gender"),
                anonymous = rs.getBoolean("anonymous")
            )
        }
}
