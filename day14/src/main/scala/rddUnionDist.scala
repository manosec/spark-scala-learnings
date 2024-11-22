import org.apache.spark.sql.SparkSession

object rddUnionDist {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Rdd Union").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val rdd1 = sprk_ctx.parallelize(Seq(1, 2, 3, 4, 5))
      val rdd2 = sprk_ctx.parallelize(Seq(4, 5, 6, 7, 8))

      // Union Transformation
      val unionRDD = rdd1.union(rdd2).distinct()

      // Collect and print the result
      unionRDD.collect().foreach(println)

    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
