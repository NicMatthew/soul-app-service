package com.soul.app.soul_app_service.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.ZonedDateTime

@Service
class JwtService {
    @Value("\${secret.key}")
    private lateinit var secretKey: String


    fun generateToken(userId: Int): String {
        val token = JWT.create()
            .withClaim("sub", userId)
            .withIssuer("issuer")
            .withIssuedAt(Date(System.currentTimeMillis()))
            .withExpiresAt(Date(System.currentTimeMillis() + 30 * 60 * 1000))
            .sign(Algorithm.HMAC256(secretKey))

        return token
    }

    fun verifyToken(token: String): Boolean {
        val jwt = JWT.require(Algorithm.HMAC256(secretKey)).build()
        return try {
            jwt.verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }
    fun getUserIdFromToken(token: String): Int? {
        val jwt = JWT.require(Algorithm.HMAC256(secretKey)).build()
        try {
            val userIdClaim = jwt.verify(token).getClaim("sub")
            return userIdClaim.asInt()
        } catch (e: Exception) {
            return null
        }
    }
}