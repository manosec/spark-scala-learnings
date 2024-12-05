import org.apache.hadoop.shaded.org.checkerframework.checker.units.qual.Current
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{broadcast, current_timestamp, log}


object _1sparkBroadcast {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Spark Broadcast").master("local[4]")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/manoranjann/spark-scala-serviceaccount.json")
      .getOrCreate()
    val sprk_ctx = spark.sparkContext

    try {

      val userDf = spark.read.format("csv").option("header", true).option("inferSchema", true).load("gs://artifacts_spark/user_details.csv")
      val transactionDf = spark.read.format("csv").option("header", true).option("inferSchema", true).load("gs://artifacts_spark/transaction_logs.csv")


      //Broadcast
      val startTime  = System.currentTimeMillis() / 1000.0;
      val broadcastDf = broadcast(userDf)
      val joinedBroadDf = transactionDf.join(broadcastDf, "user_id")
      println(joinedBroadDf.count())
      val endTime = System.currentTimeMillis() / 1000.0;

      //Without Broadcast
      val startOps = System.currentTimeMillis() / 1000.0
      val joinDf = transactionDf.join(userDf, "user_id")
      println(joinDf.count())
      val endOps = System.currentTimeMillis() / 1000.0


      println(s"Time taken to join the data without broadcast: ${endOps-startOps}")
      println(s"Time taken to join the data with broadcast: ${endTime - startTime}");

    } finally {
//      Thread.currentThread().join()
      spark.stop()
    }
  }
}
