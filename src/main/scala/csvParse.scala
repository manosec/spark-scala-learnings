import org.apache.spark.sql.SparkSession

object csvParse {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("CSV Parse").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {
      // Sample list of CSV strings
      val data = List(
        "John,25",
        "Alice,17",
        "Bob,19",
        "Carol,15",
        "David,21"
      )

      // Create an RDD from the list of strings
      val rdd = sprk_ctx.parallelize(data)

      //Transformation
      val filteredRdd = rdd
        .map(row => {
          val fields = row.split(",") // Split by comma
          (fields(0), fields(1).toInt) // Create a tuple (name, age)
        })
        .filter(record => record._2 >= 18) // Filter based on age

      //Action
      filteredRdd.collect().foreach(record => println(s"Name: ${record._1}, Age: ${record._2}"))

    } finally {
      spark.stop()
    }
  }


  // Stop the Spark session

}
