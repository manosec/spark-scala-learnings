import org.apache.spark.sql.SparkSession

object groupAndSum {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Group And Sum").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val data = sprk_ctx.parallelize(Seq(("a", 3), ("b", 5), ("a", 2), ("b", 1), ("c", 4)))

      //Transformation
      val groupedData = data.groupByKey()
      val sumByKey = groupedData.mapValues(values => values.sum)

      //Actions
      sumByKey.collect().foreach(println)

    } finally {
      spark.stop()
    }

  }


}
