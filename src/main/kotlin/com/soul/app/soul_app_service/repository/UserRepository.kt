package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.model.User
import com.soul.app.soul_app_service.util.executePreparedStatementQuery
import com.soul.app.soul_app_service.util.getConnectionOrThrows
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.lang.reflect.Array.setBoolean
import java.sql.Date

@Repository
class UserRepository(private val jdbcTemplate: JdbcTemplate) {
    fun saveUser(user: User): String {
        val query = "INSERT INTO public.users (email, password_hash, username, phone, role, profile_picture, dob, gender, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING id"
        return executePreparedStatementQuery(
            jdbcTemplate.getConnectionOrThrows(),
            query,
            {
                setString(1, user.email)
                setString( 2, user.password_hash)
                setString( 3, user.username)
                setString( 4, user.phone)
                setString(5, user.role)
                setString( 6, user.profile_picture)
                setDate( 7, user.dob)
                setString( 8, user.gender)
            },
            RowMapper { rs, _ ->
                val id = rs.getInt("id")
                return@RowMapper "$id"
            }
        ).first()
    }

    fun getUserByEmail(email: String): User? {
        val query = "SELECT * FROM public.users WHERE email = ?"
        return jdbcTemplate.queryForObject(
            query,
            RowMapper { rs, _ ->
                User(
                    id = rs.getInt("id"),
                    email = rs.getString("email"),
                    password_hash = rs.getString("password_hash"),
                    username = rs.getString("username"),
                    phone = rs.getString("phone"),
                    role = rs.getString("role"),
                    profile_picture = rs.getString("profile_picture"),
                    dob = rs.getDate("dob"),
                    gender = rs.getString("gender")
                )
            },
            email
        )
    }
    fun getUserByUsername(username: String): User? {
        val query = "SELECT * FROM public.users WHERE username = ?"
        return jdbcTemplate.queryForObject(
            query,
            RowMapper { rs, _ ->
                User(
                    id = rs.getInt("id"),
                    email = rs.getString("email"),
                    password_hash = rs.getString("password_hash"),
                    username = rs.getString("username"),
                    phone = rs.getString("phone"),
                    role = rs.getString("role"),
                    profile_picture = rs.getString("profile_picture"),
                    dob = rs.getDate("dob"),
                    gender = rs.getString("gender")
                )
            },
            username
        )
    }

    fun getUserById(id: Int): User? {
        val query = "SELECT * FROM public.users WHERE id = ?"
        return jdbcTemplate.queryForObject(
            query,
            RowMapper { rs, _ ->
                User(
                    id = rs.getInt("id"),
                    email = rs.getString("email"),
                    password_hash = rs.getString("password_hash"),
                    username = rs.getString("username"),
                    phone = rs.getString("phone"),
                    role = rs.getString("role"),
                    profile_picture = rs.getString("profile_picture"),
                    dob = rs.getDate("dob"),
                    gender = rs.getString("gender")
                )
            },
            id
        )
    }

    fun updateUser(user: User): Int {
        val sql = """
        UPDATE public.users
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


    fun deleteUser(id: Int): String {
        val query = "DELETE FROM public.users WHERE id = ? RETURNING id"
        return executePreparedStatementQuery(
            jdbcTemplate.getConnectionOrThrows(),
            query,
            {
                setInt( 1, id)
            },
            RowMapper { rs, _ ->
                val id = rs.getInt("id")
                return@RowMapper "$id"
            }
        ).first()
    }
}