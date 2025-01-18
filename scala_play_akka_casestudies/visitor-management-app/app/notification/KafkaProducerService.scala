package services

import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.Json
import config.KafkaConfig
import model.VisitorNotification
import scala.util.{Success, Failure, Try}
import play.api.Logger

@Singleton
class KafkaProducerService @Inject()() {
  private val logger = Logger(this.getClass)
  
  private val props = new java.util.Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.bootstrapServers)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  
  private val producer = new KafkaProducer[String, String](props)
  
  def sendVisitorNotification(notification: VisitorNotification): Unit = {
    Try {
      val jsonString = Json.toJson(notification).toString()
      val record = new ProducerRecord[String, String](
        KafkaConfig.visitorTopic,
        notification.visitorId.toString,
        jsonString
      )
      println(record)
      producer.send(record)
    } match {
      case Success(_) => 
        logger.info(s"Successfully sent notification for visitor ${notification.visitorId}")
      case Failure(e) => 
        logger.error(s"Failed to send notification for visitor ${notification.visitorId}", e)
        println(e)
    }
  }
  
  def close(): Unit = {
    producer.close()
  }
}