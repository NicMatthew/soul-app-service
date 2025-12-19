package com.soul.app.soul_app_service.repository

import com.soul.app.soul_app_service.util.executePreparedStatementQuery
import com.soul.app.soul_app_service.util.getConnectionOrThrows
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement

@Repository
class TestRepository(private val jdbcTemplate: JdbcTemplate) {
    fun testRepository(): String {
        val query = "select * from testdb where id = ?"
        val preparedStatement: PreparedStatement.() -> Unit = {
            setString(1, "test")
        }
        val rowMapper = RowMapper { rs, _ ->
            return@RowMapper rs.getString("id")
        }
        return executePreparedStatementQuery(
            jdbcTemplate.getConnectionOrThrows(),
            query,
            preparedStatement,
            rowMapper
        ).first()
    }

}