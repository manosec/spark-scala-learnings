import akka.actor.Actor
import models.Message
import kafka.kafkaInit



object Constants {
  val NETWORK_MESSAGE_TOPIC = "network-message"
  val APP_MESSAGE_TOPIC = "app-message"
  val CLOUD_MESSAGE_TOPIC = "cloud-message"
  val CONSOLIDATED_MESSAGES_TOPIC = "consolidated-messages"
}

//Consumer that listens to the network messages topic and produces them to the message gatherer

class NetworkMessageConsumer extends Actor {
  def receive = {
    case message: Message =>
      messageGatherer ! message
  }
}

class AppMessageConsumer extends Actor {
  def receive = {
    case message: Message =>
      messageGatherer ! message
  }
}

class CloudMessageConsumer extends Actor {
  def receive = {
    case message: Message =>
      messageGatherer ! message
  }
}


//Service that gathers messages from the consumers and produces them to the consolidated messages topic
class messageGatherer(producer: KafkaProducer[String, String]) extends Actor {
  override def receive = {
    case pm: processMessage =>
    val record = new ProducerRecord[String, String](Constants.CONSOLIDATED_MESSAGES_TOPIC, pm.messageKey, pm.message)
      producer.send(record)
    case _ =>
      println("Received unknown message")
  }
}





object messageConsumers {

    val actorSystem = ActorSystem("messageConsumers")

    val producer: KafkaProducer[String, String] = kafkaInit.createProducer()


    val networkMessageConsumer = actorSystem.actorOf(Props[NetworkMessageConsumer], "networkMessageConsumer")
    val appMessageConsumer = actorSystem.actorOf(Props[AppMessageConsumer], "appMessageConsumer")
    val cloudMessageConsumer = actorSystem.actorOf(Props[CloudMessageConsumer], "cloudMessageConsumer")
    val messageGatherer = actorSystem.actorOf(Props(new messageGatherer(producer)), "messageGatherer")

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost"+":9092")
      .withGroupId("test-consumer-group")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    // Create and start the consumers (i.e, messageListeners)
    def listeners(topic: String, listener: ActorRef): Unit = {
      Consumer
        .plainSource(consumerSettings, Subscriptions.topics(topic))
        .map{ record => record.value().parseJson.convertTo[ProcessMessage] }
        .runWith(
          Sink.actorRef[ProcessMessage](
            ref = listener,
            onCompleteMessage = "complete",
            onFailureMessage = (throwable: Throwable) => s"Exception encountered"
          )
        )
    }

    // Configure listeners
    listeners(Constants.NETWORK_MESSAGE_TOPIC, networkListener)
    listeners(Constants.APP_MESSAGE_TOPIC, appListener)
    listeners(Constants.CLOUD_MESSAGE_TOPIC, cloudListener)


}