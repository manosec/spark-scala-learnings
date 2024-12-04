import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{broadcast, col, from_json, coalesce, lit}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructField, StructType}

object _5kafkaStreamStore {
  def main(args: Array[String]): Unit = {

    val topic = "orders"

    val spark = SparkSession.builder()
      .appName("User Details CSV To GCS")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/manoranjann/spark-scala-serviceaccount.json")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN") // set log level to warn

    // dataframe
    val userDetailsDF = spark.read.format("csv").option("header", true).option("inferSchema", true).load("src/main/data/user_details.csv")
    userDetailsDF.show(10)

    // kafkaStream dataframe
    val kafkaStreamDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .load()

    val ordersSchema = StructType(Seq(
      StructField("order_id", IntegerType),
      StructField("user_id", IntegerType),
      StructField("amount", DoubleType)
    ))


    val parsedDF = kafkaStreamDF
      .selectExpr("CAST(value AS STRING) as json_string")
      .select(from_json(col("json_string"), ordersSchema).as("data"))
      .select("data.order_id", "data.user_id", "data.amount")

    // Transformation
    val enrichedDF = parsedDF
      .join(broadcast(userDetailsDF), Seq("user_id"), "left_outer")
      .select(
        col("order_id"),
        col("user_id"),
        col("amount"),
        coalesce(col("name"), lit("UNKNOWN")).alias("name"),
        coalesce(col("age"), lit(0)).alias("age"),
        coalesce(col("email"), lit("NOT_AVAILABLE")).alias("email")
      )


    val query = enrichedDF.writeStream
      .outputMode("append")
      .format("json")
      .option("path", "gs://artifacts_spark/streamSave/")
      .option("checkpointLocation", "gs://artifacts_spark/streamSaveEnriched")
      .start()

    query.awaitTermination()
  }
}