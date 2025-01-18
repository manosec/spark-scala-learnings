package config

import com.typesafe.config.ConfigFactory

object KafkaConfig {
  private val config = ConfigFactory.load()
  
  val bootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "")
  val taskAssignmentTopic: String = sys.env.getOrElse("KAFKA_EVENT_MANAGEMENT_TOPIC", "")
}
