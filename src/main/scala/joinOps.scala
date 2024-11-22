import org.apache.spark.sql.SparkSession

object joinOps {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Join Ops").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val namesRDD = sprk_ctx.parallelize(Seq((1, "Alice"), (2, "Bob"), (3, "Charlie")))
      val scoresRDD = sprk_ctx.parallelize(Seq((1, 85), (2, 90), (4, 75)))

      // Transform
      val joinedRDD = namesRDD.join(scoresRDD)


      val result = joinedRDD.map {
        case (id, (name, score)) => (id, name, score)
      }

      // Action
      result.collect().foreach(println)

    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
