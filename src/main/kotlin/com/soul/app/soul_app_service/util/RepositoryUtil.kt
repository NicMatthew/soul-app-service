package com.soul.app.soul_app_service.util

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.RowMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.UUID

fun commitTransaction(conn: Connection, transactionUnit: () -> Int): Int {
    try {
        conn.autoCommit = false
        val updated =  transactionUnit.invoke()
        conn.commit()
        return updated
    } catch (exception: Exception){
        conn.rollback()
        throw exception
    } finally {
        conn.autoCommit = true
        conn.close()
    }
}

/**
 * create a preparedStatement of a given query, then [PreparedStatement.executeQuery].
 * Automatically close resources with Kotlin's [use]. See also [executePreparedStatementQuery]
 *
 * Be careful not to pass any non-disposable/long-lived references inside [configurePrepareStatement]
 * or else it might cause memory leak, because it is just another callback parameter
 *
 * @param connection interface to the database
 * @param query query string, use '?' character to denote bind parameter
 * @param configurePrepareStatement actions done to the created preparedStatement. Bind parameters here
 * @param rowMapper to operate with each row of the result.
 * It is advisable to return empty list in the implementation when no result can be inferred from the result set
 * @return [List]<T>
 */
fun <T>executePreparedStatementQuery(
    connection: Connection,
    query: String,
    configurePrepareStatement: PreparedStatement.() -> Unit,
    rowMapper: RowMapper<T>
): List<T> {
    val data = mutableListOf<T>()
    connection.use { conn ->
        conn.prepareStatement(query).use { ps ->
            ps.configurePrepareStatement()
            ps.executeQuery().use { rs ->
                while(rs.next()) {
                    rowMapper.mapRow(rs, rs.row)?.also {data.add(it)}
                }
            }
        }
    }
    return data
}

/**
 * create a preparedStatement of a given query, then [PreparedStatement.executeQuery].
 * Automatically close resources with Kotlin's [use]
 *
 * Be careful not to pass any non-disposable/long-lived references inside [configurePrepareStatement]
 * or else it might cause memory leak, because it is just another callback parameter
 * @param connection interface to the database
 * @param query query string, use '?' character to denote bind parameter
 * @param configurePrepareStatement actions done to the created preparedStatement. Bind parameters here
 * @param resultSetExtractor to operate with the result.
 * It is advisable to return null in the implementation when no result can be inferred from the result set
 * @return T?
 */
fun <T>executePreparedStatementQuery(
    connection: Connection,
    query: String,
    configurePrepareStatement: PreparedStatement.() -> Unit,
    resultSetExtractor: ResultSetExtractor<T>
): T? {
    return connection.use { conn ->
        conn.prepareStatement(query).use { ps ->
            ps.configurePrepareStatement()
            ps.executeQuery().use { rs ->
                resultSetExtractor.extractData(rs)
            }
        }
    }
}

fun prepareInStatementContainer(numberOfData: Int): String {
    if (numberOfData <= 0 ) return "''"
    val stringBuilder = StringBuilder()
    for (i in 1..numberOfData) {
        stringBuilder.append("?")
        if (i < numberOfData) {
            stringBuilder.append(",")
        }
    }
    return stringBuilder.toString()
}

/**
 *  Returns JDBC data source connection or throws exception
 *  @return java.sql.Connection
 *  @throws java.sql.SQLException if a database access error occurs
 *  @throws java.sql.SQLTimeoutException if connection attempt has timed out
 *  @throws DataSourceException if DataSource is null
 */
fun JdbcTemplate.getConnectionOrThrows(): Connection {
    return dataSource?.connection ?: throw Exception("Data Source Tidak Valid")
}


fun escapeSpecialCharacters(input: String): String {
    return input.replace(Regex("([_%])"), "\\\\$1")
}

fun generateUuid(): String = UUID.randomUUID().toString().replace("-", "").substring(0,30)