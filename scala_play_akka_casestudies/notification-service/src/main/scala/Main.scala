import akka.actor.{ActorRef, ActorSystem, Props}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import spray.json._
import config.KafkaConfig
import actors.{EventManagementActor, VisitorManagementActor}
import models.{EventManagementConsumerModel, VisitorManagementConsumerModel}
import akka.actor.typed.SupervisorStrategy
import akka.stream.CompletionStrategy

object MainApp {
  def main(args: Array[String]): Unit = {   
    // Configure logging to suppress debug messages
    val loggerContext = org.slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[ch.qos.logback.classic.LoggerContext]
    val kafkaLogger = loggerContext.getLogger("org.apache.kafka")
    kafkaLogger.setLevel(ch.qos.logback.classic.Level.INFO)

    implicit val system = ActorSystem("Notification-System")

    implicit val materializer: Materializer = ActorMaterializer()(system)

    println("Starting Kafka consumer application...")

    // Add shutdown hook
    sys.addShutdownHook {
      system.terminate()
    }
    
    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      // Configure Kafka bootstrap servers (broker locations)
      .withBootstrapServers(KafkaConfig.bootstrapServers)
      .withGroupId(KafkaConfig.groupId)
      // Add stability-related settings
      .withProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000")  // 60 seconds
      .withProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000") // 3 seconds
      .withProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000") // 5 minutes
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    // Create actor references
    val eventManagementListener = system.actorOf(Props[EventManagementActor], "eventManagementActor")
    val visitorManagementListener = system.actorOf(Props[VisitorManagementActor], "visitorManagementActor")
    
    println(s"Kafka bootstrap servers: ${KafkaConfig.bootstrapServers}")

    // Uncomment this if you want to listen to both topics
    startListener(consumerSettings, KafkaConfig.eventManagementTopic, eventManagementListener)
    startListener(consumerSettings, KafkaConfig.visitorManagementTopic, visitorManagementListener)

  }
  private def startListener(consumerSettings: ConsumerSettings[String, String], topic: String, listener: ActorRef)
                           (implicit materializer: Materializer): Unit = {
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
      .map { record => 
        
        try {
          println(s"[${topic}] Received: ${record.value()}")
          val parsed = topic match {
            case KafkaConfig.eventManagementTopic => 
              record.value().parseJson.convertTo[EventManagementConsumerModel]
            case KafkaConfig.visitorManagementTopic => 
              record.value().parseJson.convertTo[VisitorManagementConsumerModel]
            case _ => 
              throw new IllegalArgumentException(s"Unsupported topic: $topic")
          }
          Right(parsed)
        } catch {
          case ex: Exception =>
            println(s"[${topic}] Parse error: ${ex.getMessage}")
            Left(ex)
        }
      }
      .recover {
        case ex: Exception =>
          println(s"[${topic}] Stream error: ${ex.getMessage}")
          Left(ex)
      }
      .runWith(Sink.actorRefWithBackpressure(
        listener,
        onInitMessage = "init",
        ackMessage = "ack",
        onCompleteMessage = "complete",
        onFailureMessage = { ex: Throwable => 
          println(s"[${topic}] Fatal error: ${ex.getMessage}")
          "failure"
        }
      ))
  }


}
