import org.apache.spark.sql.SparkSession
import org.apache.spark.TaskContext


object cartesianInt {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Cartesian Integer Product").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val rdd1 = sprk_ctx.parallelize(Seq(1, 2, 3))
      val rdd2 = sprk_ctx.parallelize(Seq(4, 5))

      // Cartesian operation
      val cartesianResult = rdd1.cartesian(rdd2)

      // Map Transformation
      val productResult = cartesianResult.map { case (a, b) => a * b }

      // Collect Action
      productResult.collect().foreach(println)
    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
