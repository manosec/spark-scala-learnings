import org.apache.spark.sql.SparkSession

object avgScore {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Average Score").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {
      val data = List((1, 34), (2, 65), (3, 78), (4, 75), (5, 89))

      val data_paritioned = sprk_ctx.parallelize(data)

      // Map Transformation
      val mapProcess = data_paritioned.map(point => (point._2, 1))

      //Reduce  Action
      val sumData = mapProcess.reduce((a, b) => (a._1 + b._1, a._2 + b._2))

      val avgoutput = sumData._1.toDouble / sumData._2.toDouble

      print(avgoutput)
    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
