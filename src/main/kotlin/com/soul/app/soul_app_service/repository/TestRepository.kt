package com.soul.app.soul_app_service.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement

@Repository
class TestRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun testRepository(): String {
        val sql = "SELECT id FROM testdb WHERE id = ?"

        return jdbcTemplate.query(
            sql,
            RowMapper { rs, _ -> rs.getString("id") },
            "test"
        ).first()
    }
}
