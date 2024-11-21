import org.apache.spark.sql.SparkSession

object avgScore {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Average Score").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {
      val data = List((1, 34), (2, 65), (3, 78), (4, 75), (5, 89))

      // Parallelize the data into an RDD
      val data_paritioned = sprk_ctx.parallelize(data)

      // Map each item to a (value, 1) pair for counting
      val mapProcess = data_paritioned.map(point => (point._2, 1))

      // Reduce to get the sum of values and the count of elements
      val sumData = mapProcess.reduce((a, b) => (a._1 + b._1, a._2 + b._2))

      // Compute the average, ensure floating-point division
      val avgoutput = sumData._1.toDouble / sumData._2.toDouble

      // Use collect to trigger the job and get the result
      val result = sprk_ctx.parallelize(Seq(avgoutput)).collect()


    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
