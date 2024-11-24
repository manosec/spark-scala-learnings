import org.apache.spark.sql.SparkSession

object execAnalysis_3 {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Executor Analysis")
      .master("local[*]")
      .config("spark.executor.instances", "5") //This does not taking any effect have to look for executor config
      .getOrCreate()

    val sprk_ctx = spark.sparkContext

    val loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus sit amet."

    val fillData = Seq.fill(100000000)(loremIpsum)

    try {

      val data = sprk_ctx.parallelize(fillData, 10)

      //Transform Ops
      val words = data.flatMap(lines => lines.split(" "))

      val mappedDataPairs = words.map(point => {
        (point, 1)
      })

      //Reduce Ops
      val reducesPairs = mappedDataPairs.reduceByKey(_+_)

      reducesPairs.take(10).foreach(println)


    } finally {
      Thread.currentThread().join()
      spark.stop()
    }
  }

}
