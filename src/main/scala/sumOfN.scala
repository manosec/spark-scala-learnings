import org.apache.spark.sql.SparkSession

object sumOfN {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Sum Of N").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val numbersRdd = sprk_ctx.parallelize(1 to 100)

      //Action
      val sum = numbersRdd.reduce((a, b) => a + b)
      // Print the result
      println(s"The sum of integers from 1 to 100 using reduce is: $sum")

    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
