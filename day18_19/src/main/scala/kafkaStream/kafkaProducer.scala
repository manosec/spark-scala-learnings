package kafkaStream
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties

import scala.io.Source
import com.google.gson.{Gson, JsonParser}

import scala.collection.convert.ImplicitConversions.`iterator asScala`

object kafkaProducer {
  def main(args: Array[String]): Unit = {

    val producer: KafkaProducer[String, String] = {
      val props = new Properties()
      props.put("bootstrap.servers", "localhost:9092")
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      new KafkaProducer[String, String](props)
    }

    try {
      val kafka_topic = "user_transaction_details"
      val jsonFilePath = "src/main/data/transactions.json"

      val messages = readJson(jsonFilePath)
      messages.foreach{msg =>
        producer.send((new ProducerRecord[String, String](kafka_topic, msg)))
        Thread.sleep(1000)
      }
    }
  }


  private def readJson(filePath: String): List[String] = {
    var sourceContent: Source = null
    try {
      sourceContent = Source.fromFile(filePath)
      val content = sourceContent.mkString

      val gson = new Gson()
      val elem = JsonParser.parseString(content)

      if(elem.isJsonArray) {
        elem.getAsJsonArray.iterator()
          .map(elem => gson.toJson(elem))
          .toList
      } else{
        List(gson.toJson(elem))
      }
    } catch {
      case e: Exception =>
        println(s"Error reading file: ${e.getMessage}")
        List.empty
    } finally {
      if (sourceContent != null) sourceContent.close()
    }
  }


}
