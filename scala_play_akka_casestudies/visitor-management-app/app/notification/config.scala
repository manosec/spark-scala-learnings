package config


object KafkaConfig {
  val bootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "")
  val visitorTopic: String = sys.env.getOrElse("KAFKA_VISITOR_NOTIFICATION_TOPIC", "")
}
