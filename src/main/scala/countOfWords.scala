import org.apache.spark.sql.SparkSession


object countOfWords {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Count of Words").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try{
      val data = Seq ("Hi there", "What is your name?", "Where is Mano?")

      val data_parti = sprk_ctx.parallelize(data)
      // Map Transformation
      val output = data_parti.map(point => {
        val field = point.split(" ")
        (point, field.length)
      })
      // Collect Action
      output.collect().foreach(point => {
        println(point._1, point._2)
      })

    } finally {
      spark.stop()
    }
  }
}