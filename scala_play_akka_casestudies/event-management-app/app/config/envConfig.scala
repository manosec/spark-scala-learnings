package config

import com.typesafe.config.ConfigFactory

object EnvConfig {
    val config = ConfigFactory.load()

    def getJwtUtilSecretKey: String = sys.env.getOrElse("JWT_SECRET_KEY", "")
    def getJwtUtilIssuer: String = sys.env.getOrElse("JWT_ISSUER", "")
}