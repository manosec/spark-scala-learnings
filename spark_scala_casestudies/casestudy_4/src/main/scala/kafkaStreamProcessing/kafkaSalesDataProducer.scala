package kafkaStreamProcessing

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random
import kafkaStreamProcessing.salesDataShema
import akka.stream.scaladsl.FileIO
import akka.util.ByteString
import java.nio.file.Paths
import config.config.kafka_topic


object kafkaSalesDataProducer {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("salesDataProducer")
    val bootstrapServer = "localhost:9092"
    val topic = kafka_topic

    val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServer)

    def buildSalesRecord(store: String, dept: String, date: String, weeklySales: Float, isHoliday: Boolean): salesDataShema = {
      salesDataShema(store, dept, date, weeklySales, isHoliday)
    }

//    val filePath = "src/main/datasets/train.csv"

        def buildRecord(): ProducerRecord[String, String] = {
          val store = s"${Random.nextInt(10) + 1}"  // Random store
          val dept = s"${Random.nextInt(20) + 1}"   // Random department
          val date = java.time.LocalDate.now().toString  // Current date
          val weeklySales = 1000 + Random.nextFloat() * 1000
          val isHoliday = Random.nextBoolean()           // Random holiday flag

          val salesRecord = buildSalesRecord(store, dept, date, weeklySales, isHoliday)
          val salesRecordJson = salesRecord.asJson.noSpaces
          println(s"SalesRecord: $salesRecord") // Debugging
          new ProducerRecord[String, String](topic, salesRecordJson)
        }

    val salesRecordSource = Source.tick(0.seconds, 1.seconds, ())
      .map { _ => buildRecord() }

    salesRecordSource.runWith(Producer.plainSink(producerSettings)).onComplete{
      result =>
        println(s"Stream completed: $result")
        system.terminate()
    }

  }

}


