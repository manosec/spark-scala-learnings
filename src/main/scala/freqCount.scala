import org.apache.spark.sql.SparkSession

object freqCount{
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Freq Count").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val strings = List("hello", "world", "spark", "Happy")

      val stringsRDD = spark.sparkContext.parallelize(strings)

      // Flap Map and ReducesByKey Transformation
      val charFrequencyRDD = stringsRDD
        .flatMap(_.toCharArray)
        .map(char => (char, 1))
        .reduceByKey(_ + _)

      // Collect Action
      println("Character frequencies:")
      charFrequencyRDD.collect().foreach{ case (char, count) =>
        println(s"$char -> $count")
      }

    } finally {
      spark.stop()
    }

  }


}
