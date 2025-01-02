  package kafkaStreamProcessing

  import org.apache.spark.sql.{DataFrame, Dataset, Row, SaveMode, SparkSession}
  import org.apache.spark.sql.functions._
  import org.apache.spark.sql.streaming.Trigger
  import org.apache.spark.sql.Encoders
  import org.apache.spark.sql.execution.streaming.state.StateStoreMetrics
  import org.apache.spark.storage.StorageLevel
  import kafkaStreamProcessing.salesDataShema
  import config.config.serviceAccountPath

  object kafkaStreamConsumer {

    val spark = SparkSession.builder().appName("Kafka Sales Data Processing").master("local[*]")
      .config("spark.driver.bindAddress","localhost")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", serviceAccountPath)
      .getOrCreate()

    val sprk_ctx = spark.sparkContext

    //Main data path
    val featureDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/walmart-recruiting-store-sales-forecasting/features.csv"
    val storesDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/walmart-recruiting-store-sales-forecasting/stores.csv"
    val kafkaMainDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/kafka/train"
    val kafkaEnrichedDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/kafka/stream_enriched_data_csv"

    //Agg data path
    val storeMetricsPath = "gs://artifacts_spark/de-casestudy/usecase-4/processed_output/store_level"
    val kafkaKStoreMetricsPath = "gs://artifacts_spark/de-casestudy/usecase-4/kafka/store_level"
    val deptMetricsPath = "gs://artifacts_spark/de-casestudy/usecase-4/processed_output/department_level"
    val holidayMetricsPath = "gs://artifacts_spark/de-casestudy/usecase-4/processed_output/holiday_vs_non_holiday"


    // Load from GCS
    val featureDf = spark.read
      .option("header", "true") // Adjusted for datasets saved with headers
      .option("inferSchema", "true")
      .csv(featureDataPath)

    val storeDf = spark.read
      .option("header", "true") // Adjusted for datasets saved with headers
      .option("inferSchema", "true")
      .csv(storesDataPath)


    val storeMetricsDf = spark.read.json(storeMetricsPath)

    val deptMetricsDf = spark.read.json(deptMetricsPath)

    val holidayMetricsDf = spark.read.json(holidayMetricsPath)


    def main(args: Array[String]): Unit = {

      try {
        //Remove the null and NaN value from the dataset and cache
        val featuresDf = featureDf.na.drop("any", Seq("Store", "Date")).cache()

        //Remove the null and NaN value from the dataset and broadcast
        val storesDf = broadcast(storeDf.na.drop("any", Seq("Store", "Type", "Size")))

        //Kafka Config
        val kafkaServer = "localhost:9092"
        val topic = "realtime-sales-data"

        val kafkaDf = spark.readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", kafkaServer)
          .option("subscribe", topic)
          .option("startingOffsets", "earliest")
          .load()

        //Define Kafka data schema
        implicit val salesRecordSchema = Encoders.product[salesDataShema]

        //Parse the Kafka data to create DataFrame
        val salesDataDf = kafkaDf.selectExpr("CAST(value AS STRING) as json")
          .select(from_json(col("json"), salesRecordSchema.schema).as("data"))
          .select("data.*")
          .na.fill(Map(
            "is_Holiday" -> false, // Fill missing values with false
            "weeklySales" -> 0.0f // Fill missing values with 0
          ))
          .select(
            col("store").alias("Store"),
            col("dept").alias("Dept"),
            col("date").alias("Date"),
            col("weeklySales").alias("Weekly_Sales"),
            col("is_Holiday").alias("IsHoliday")
          )


        // filter out records where Weekly_Sales is negative and remove null values
        val filteredSalesDataDf = salesDataDf.filter(col("Weekly_Sales") >= 0).na.drop("any", Seq("Store", "Dept", "Weekly_Sales", "Date"))

        //Fetch and process the new data into existing enriched data from GCS
        val processedData  =  filteredSalesDataDf.writeStream.trigger(Trigger.ProcessingTime("10 seconds"))
          .foreachBatch{ (batchData: Dataset[Row], batchId: Long) =>
            val newBatchData = batchData.cache()

            //Add the new sales to the existing data in GCS
            newBatchData.write.mode("append").option("header", "true").csv(kafkaMainDataPath)

            enrichAndAggNewData( newBatchData, featuresDf, storesDf)

          }
          .start()

        processedData.awaitTermination()
      } finally {
        spark.stop()
      }

    }

    def enrichAndAggNewData(newBatchData: DataFrame, featuresDf: DataFrame, storesDf: DataFrame): Unit = {

      val newEnrichedData =  newBatchData
        .join(featuresDf.withColumnRenamed("IsHoliday", "Feature_IsHoliday"), Seq("Store", "Date"), "left")
        .join(storesDf, Seq("Store"), "left")

      newEnrichedData.show(10)

      newEnrichedData.write.mode("append").partitionBy("Store").option("header", "true").parquet(kafkaEnrichedDataPath)

      //Update store wise metrics
      val updatedStoreMetrics = computeNewDataStoreMetrics(newEnrichedData, storeMetricsDf).persist(StorageLevel.MEMORY_ONLY)
      updatedStoreMetrics.show(10)
      updatedStoreMetrics.write.mode("overwrite").json(kafkaKStoreMetricsPath)

    }

    def computeNewDataStoreMetrics(newEnrichedData: DataFrame, storeMetrics: DataFrame): DataFrame = {
      val newStoreMetrics = newEnrichedData.groupBy("Store")
        .agg(
          sum("Weekly_Sales").alias("New_Total_Weekly_Sales"),
          avg("Weekly_Sales").alias("New_Average_Weekly_Sales"),
          count("*").alias("New_Data_Count")
        )

      val updatedStoreMetrics = if (storeMetrics != null) {
        newStoreMetrics.join(storeMetrics, Seq("Store"), "outer")
          .select(
            coalesce(col("Store"), lit("Unknown")).alias("Store"),
            (coalesce(col("New_Total_Weekly_Sales"), lit(0.0)) + coalesce(col("Total_Weekly_Sales"), lit(0.0))).alias("Total_Weekly_Sales"),
            (
              ((coalesce(col("New_Average_Weekly_Sales"), lit(0.0)) * coalesce(col("New_Data_Count"), lit(0))) +
                (coalesce(col("Average_Weekly_Sales"), lit(0.0)) * coalesce(col("Data_Count"), lit(0)))) /
                (coalesce(col("New_Data_Count"), lit(0)) + coalesce(col("Data_Count"), lit(0)))
              ).alias("Average_Weekly_Sales"),
            (coalesce(col("New_Data_Count"), lit(0)) + coalesce(col("Data_Count"), lit(0))).alias("Data_Count")
          )
      } else {
        newStoreMetrics.select(
          col("Store"),
          col("New_Total_Weekly_Sales").alias("Total_Weekly_Sales"),
          col("New_Average_Weekly_Sales").alias("Average_Weekly_Sales"),
          col("New_Data_Count").alias("Data_Count")
        )
      }
      updatedStoreMetrics
    }
  }
