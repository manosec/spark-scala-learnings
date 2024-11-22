import org.apache.spark.sql.SparkSession

object groupAndSum {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Group And Sum").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val data = sprk_ctx.parallelize(Seq(("a", 3), ("b", 5), ("a", 2), ("b", 1), ("c", 4)))

      //Transformation
      val groupedData = data.groupByKey() // This groups the data by key
      val sumByKey = groupedData.mapValues(values => values.sum) // Sum the values for each key

      //Actions
      sumByKey.collect().foreach(println)

    } finally {
      spark.stop()
    }

  }


}
