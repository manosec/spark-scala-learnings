import org.apache.spark.sql.SparkSession

object filterOutEven {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Filter out Even").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val range = 1 to 100
      val paritioned = sprk_ctx.parallelize((range))

      val output = paritioned.filter( _ % 2 != 0)

      output.collect().foreach(num => {
        println(num)
      })
    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
