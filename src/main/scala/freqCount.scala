import org.apache.spark.sql.SparkSession

object freqCount{
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Freq Count").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      // Step 2: Sample collection of strings
      val strings = List("hello", "world", "spark", "Happy")

      // Step 3: Create an RDD from the list of strings
      val stringsRDD = spark.sparkContext.parallelize(strings)

      // Step 4: Count the frequency of each character
      val charFrequencyRDD = stringsRDD
        .flatMap(_.toCharArray)         // Convert each string into a sequence of characters
        .map(char => (char, 1))         // Map each character to a tuple (char, 1)
        .reduceByKey(_ + _)            // Reduce by key to sum the occurrences of each character

      // Step 5: Collect and print the result
      println("Character frequencies:")
      charFrequencyRDD.collect().foreach{ case (char, count) =>
        println(s"$char -> $count")
      }

    } finally {
      spark.stop()
    }

  }


}
