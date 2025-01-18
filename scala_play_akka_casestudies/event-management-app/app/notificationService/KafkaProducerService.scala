package notificationService

import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.Json
import config.KafkaConfig
import models.TaskAssignmentNotification
import scala.util.{Success, Failure, Try}
import play.api.Logging
import scala.concurrent.ExecutionContext

@Singleton
class KafkaProducerService @Inject()(implicit ec: ExecutionContext) extends Logging {
  
  private val props = new java.util.Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.bootstrapServers)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  
  private val producer = new KafkaProducer[String, String](props)
  
  def sendTaskAssignmentNotification(notification: TaskAssignmentNotification): Unit = {
    Try {
      val jsonString = Json.toJson(notification).toString()
      val record = new ProducerRecord[String, String](
        KafkaConfig.taskAssignmentTopic,
        notification.taskId.toString,
        jsonString
      )
      
      producer.send(record)
    } match {
      case Success(_) => 
        logger.info(s"Successfully sent notification for task ${notification.taskId}")
      case Failure(e) => 
        logger.error(s"Failed to send notification for task ${notification.taskId}", e)
    }
  }
  
  def close(): Unit = {
    producer.close()
  }
}