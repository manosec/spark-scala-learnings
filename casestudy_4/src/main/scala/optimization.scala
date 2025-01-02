import org.apache.hadoop.shaded.org.checkerframework.checker.units.qual.Current
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{broadcast, lag, desc, col, sum, avg, count}
import org.apache.spark.sql.expressions.Window
import .config.serviceAccountPath

object optimization {
  def main(args: Array[String]):Unit = {

    val spark = SparkSession.builder().appName("Walmart Sales Analytics").master("local[*]")
      .config("spark.driver.bindAddress","localhost")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", serviceAccountPath)
      .getOrCreate()

    val sprk_ctx = spark.sparkContext
    try {

      // 1. Data Preparation
      val featureDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/walmart-recruiting-store-sales-forecasting/features.csv"
      val storesDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/walmart-recruiting-store-sales-forecasting/stores.csv"
      val mainDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/walmart-recruiting-store-sales-forecasting/train.csv"

      val featureDf = spark.read.format("csv").option("header", true).option("inferSchema", true).load(featureDataPath)
      val storeDf = spark.read.format("csv").option("header", true).option("inferSchema", true).load(storesDataPath)
      val mainDf = spark.read.format("csv").option("header", true).option("inferSchema", true).load(mainDataPath)


      // Remove records with missing values in Store, Dept, Weekly_Sales, and Date columns
      val validatedTrainDF = mainDf.na.drop("any", Seq("Store", "Dept", "Weekly_Sales", "Date"))
      val validatedStoresDF = storeDf.na.drop("any", Seq("Store", "Type", "Size"))
      val validatedFeaturesDF = featureDf.na.drop("any", Seq("Store", "Date"))

      // Filter out records where Weekly_Sales is negative
      val filteredTrainDF = validatedTrainDF.filter(col("Weekly_Sales") >= 0)

      // Cache the features and stores DataFrames for repeated use
      validatedFeaturesDF.cache()
      validatedStoresDF.cache()

      // Broadcast the stores DataFrame (small dataset)
      val broadcastedStoresDF = broadcast(validatedStoresDF)

      // Perform joins with features.csv and stores.csv on relevant keys
      val enrichedDF = filteredTrainDF
        .join(validatedFeaturesDF.withColumnRenamed("IsHoliday", "Feature_IsHoliday"), Seq("Store", "Date"), "left")
        .join(broadcastedStoresDF, Seq("Store"), "left")

      enrichedDF.cache()

      enrichedDF.show(10)

      enrichedDF.write
        .mode("overwrite") // Overwrites existing data
        .option("header", "true") // Includes header row
        .csv("gs://artifacts_spark/de-casestudy/usecase-4/processed_output/enriched_data_csv")


      // 2. Data Transformation
      val storeMetrics = enrichedDF.groupBy("Store")
        .agg(
          sum("Weekly_Sales").alias("Total_Weekly_Sales"),
          avg("Weekly_Sales").alias("Average_Weekly_Sales"),
          count("Store").alias("Data_Count")
        )
      storeMetrics.show()

      // Department-Level Metrics
      val departmentMetrics = enrichedDF.groupBy("Store", "Dept")
        .agg(
          sum("Weekly_Sales").alias("Total_Weekly_Sales"),
          avg("Weekly_Sales").alias("Average_Weekly_Sales"),
          count("Store").alias("Data_Count")
        )
      departmentMetrics.show()

      // Weekly Trends: Calculate sales difference from previous week
      val windowSpec = Window.partitionBy("Store", "Dept").orderBy("Date")
      val weeklyTrendsDF = enrichedDF
        .withColumn("Previous_Weekly_Sales", lag("Weekly_Sales", 1).over(windowSpec))
        .withColumn("Weekly_Trend", col("Weekly_Sales") - col("Previous_Weekly_Sales"))
        .select("Store", "Dept", "Date", "Weekly_Sales", "IsHoliday", "Previous_Weekly_Sales", "Weekly_Trend")


      // Holiday vs Non-Holiday Sales
      val holidaySales = enrichedDF.filter("IsHoliday = true")
        .groupBy("Store", "Dept")
        .agg(sum("Weekly_Sales").alias("Holiday_Sales"))

      val nonHolidaySales = enrichedDF.filter("IsHoliday = false")
        .groupBy("Store", "Dept")
        .agg(sum("Weekly_Sales").alias("NonHoliday_Sales"))

      val holidayComparison = holidaySales
        .join(nonHolidaySales, Seq("Store", "Dept"), "outer")
        .orderBy(desc("Holiday_Sales"))

      //Check this
      // 3. Storage Optimization
      // Partition the data by Store and Date and save it to Parquet
      val partitionedDataPath = "gs://artifacts_spark/de-casestudy/usecase-4/processed_output/partitioned_data"
      enrichedDF.write
        .mode("overwrite")
        .partitionBy("Store")
        .parquet(partitionedDataPath)

      // Optionally save the metrics as JSON for further analysis
      val storeMetricsPath = "gs://artifacts_spark/de-casestudy/usecase-4/processed_output/store_level"
      storeMetrics.write
        .mode("overwrite")
        .json(storeMetricsPath)

      val departmentMetricsPath = "gs://artifacts_spark/de-casestudy/usecase-4/processed_output/department_level"
      departmentMetrics.write
        .mode("overwrite")
        .json(departmentMetricsPath)

      val holidayVsNonHolidayMetricsPath = "gs://artifacts_spark/de-casestudy/usecase-4/processed_output/holiday_vs_non_holiday"
      holidayComparison.write
        .mode("overwrite")
        .json(holidayVsNonHolidayMetricsPath)

    } finally {
      spark.stop()
    }
  }
}
