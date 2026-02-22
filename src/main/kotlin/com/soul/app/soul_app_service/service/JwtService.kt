package com.soul.app.soul_app_service.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService {

    @Value("\${secret.key}")
    private lateinit var secretKey: String

    private val algorithm by lazy {
        Algorithm.HMAC256(secretKey)
    }

    fun generateToken(userId: Int, role: String): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withClaim("role", role)
            .withIssuer("soul-app")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 30 * 60 * 1000))
            .sign(algorithm)
    }

    fun decode(token: String): DecodedJWT {
        return JWT.require(algorithm)
            .withIssuer("soul-app")
            .build()
            .verify(token)
    }
}
