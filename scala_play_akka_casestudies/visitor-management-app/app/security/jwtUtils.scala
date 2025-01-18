package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import model.PremiseDeptRole

object JwtUtil {
  private val secretKey = SecurityConfig.jwtSecretKey
  private val algorithm = Algorithm.HMAC256(secretKey)
  private val issuer = SecurityConfig.jwtIssuer
  
  def generateToken(userId: String, role: PremiseDeptRole.PremiseDeptRole): String = {
    JWT.create()
      .withIssuer(issuer)
      .withSubject(userId)
      .withClaim("role", role.toString)
      .withIssuedAt(new Date())
      .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
      .sign(algorithm)
  }

  def validateToken(token: String): (String, String) = {
    val verifier = JWT.require(algorithm)
      .withIssuer(issuer)
      .build()
    
    val jwt = verifier.verify(token)
    (jwt.getSubject, jwt.getClaim("role").asString())
  }
}