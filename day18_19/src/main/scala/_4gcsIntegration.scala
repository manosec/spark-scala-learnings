import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col}

object _4gcsIntegration {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("GCS Integration").master("local[*]")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/manoranjann/spark-scala-serviceaccount.json")
      .getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val loadParquet = spark.read.format("parquet").option("header", true).option("inferSchema", true).load("gs://artifacts_spark/task4/input/")

      val filteredData = loadParquet.filter(col("status") === "COMPLETED")

      filteredData.write.format("parquet").mode("overwrite").save("gs://artifacts_spark/task4/output/")

    } finally {
      spark.stop()
    }
  }
}
