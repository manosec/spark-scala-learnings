package config

object KafkaConfig {
  val bootstrapServers = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "")
  val groupId = sys.env.getOrElse("KAFKA_GROUP_ID", "")
  val eventManagementTopic = sys.env.getOrElse("KAFKA_EVENT_MANAGEMENT_TOPIC", "")
  val visitorManagementTopic = sys.env.getOrElse("KAFKA_VISITOR_MANAGEMENT_TOPIC", "")    
}

