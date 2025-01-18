package jwtTokenGeneration

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import config.EnvConfig

object JwtUtil {
  private val secretKey = EnvConfig.getJwtUtilSecretKey
  private val algorithm = Algorithm.HMAC256(secretKey)
  private val issuer = EnvConfig.getJwtUtilIssuer

  def generateToken(userId: String): String = {
    JWT.create()
      .withIssuer(issuer)
      .withSubject(userId)
      .withIssuedAt(new Date())
      .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
      .sign(algorithm)
  }

  def validateToken(token: String): String = {
    val verifier = JWT.require(algorithm)
      .withIssuer(issuer)
      .build()
    
    val jwt = verifier.verify(token)
    jwt.getSubject
  }
}