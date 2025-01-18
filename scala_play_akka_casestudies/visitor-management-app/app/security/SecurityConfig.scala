package security

object SecurityConfig {
    val jwtSecretKey = sys.env.getOrElse("JWT_SECRET_KEY", "")
    val jwtIssuer = sys.env.getOrElse("JWT_VISITOR_APP_ISSUER", "")
} 