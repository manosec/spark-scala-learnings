import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col}

object _2dataCache {
   def main(args: Array[String]): Unit = {
     val spark = SparkSession.builder().appName("Data Cache Spark").master("local[*]")
       .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
       .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
       .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
       .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/manoranjann/spark-scala-serviceaccount.json")
       .getOrCreate()
     val sprk_ctx = spark.sparkContext

     try {
      val transactionDf = spark.read.format("csv").option("header", true).option("inferSchema", true).load("gs://artifacts_spark/transaction_logs.csv")

       val startTime = System.currentTimeMillis() / 1000.0;
       val filteredDf = transactionDf.filter(col("amount") >= 500);
       println(filteredDf.count())
       val endTime = System.currentTimeMillis() / 1000.0

       // Cache the mainDf
       transactionDf.cache()
       // Action to trigger the cache op
       transactionDf.count()

       val cacheStartTime = System.currentTimeMillis() / 1000.0
       val filteredCacheDf = transactionDf.filter(col("amount") >= 500)
       println(filteredCacheDf.count())
       val cacheEndPoint = System.currentTimeMillis() / 1000.0


       println((endTime - startTime))
       println((cacheEndPoint - cacheStartTime))

       //Clear Cache
       transactionDf.unpersist()
       Thread.currentThread().join()
       spark.stop()
     }
   }
}

