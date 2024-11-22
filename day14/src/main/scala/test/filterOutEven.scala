package test
import org.apache.spark.sql.SparkSession


object filterOutEven {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Filter Out Even").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {
      val data = List(1,2,3,4,5,6,7,8,9,10)

      val dataRdd = sprk_ctx.parallelize(data)

      val finalOutput = dataRdd.filter(_ % 2 != 0)

      finalOutput.collect().foreach(num => {
        println(num)
      })

    } finally {
      spark.stop()
    }
  }
}
