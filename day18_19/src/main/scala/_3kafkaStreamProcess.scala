import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{IntegerType, StructField, StructType, TimestampType}
import org.apache.spark.sql.functions.{col, current_timestamp, from_json, sum, window}
import org.apache.spark.sql.streaming.Trigger

object _3kafkaStreamProcess {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Kafka Stream Processing").master("local[*]")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/manoranjann/spark-scala-serviceaccount.json")
      .getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {
      val kafka_topic = "transactions"
      val streamDf = spark.readStream.format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", kafka_topic)
        .option("startingOffsets", "earliest")
        .load()

      val schema = StructType(Seq(
        StructField("transactionId", IntegerType, nullable = false),
        StructField("userId", IntegerType, nullable = false),
        StructField("amount", IntegerType, nullable=false),
        StructField("timestamp", TimestampType, nullable=false)
      ))

      val parsedData = streamDf
        .selectExpr("CAST(value AS STRING) as jsonString")
        .select(from_json(col("jsonString"), schema).as("data"))
        .select("data.transactionId", "data.amount")
        .withColumn("timestamp", current_timestamp())

      val avgWindow = parsedData.groupBy(window(col("timestamp"), "10 seconds" )).agg(sum("amount").alias("total_amount"))

      val query = avgWindow.writeStream
        .outputMode("update")
        .format("console")
        .trigger(Trigger.ProcessingTime("10 seconds"))
        .start()

      query.awaitTermination()

    } finally {
      spark.stop()
    }
  }


}
