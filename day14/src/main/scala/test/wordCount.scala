package test
import org.apache.spark.sql.SparkSession

object wordCount {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Count Words").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext
    try{
      val data = Seq("Hi there,", "This is Good", "This is awesome")

      val dataRdd = sprk_ctx.parallelize(data)

      val count = dataRdd.map(sentence => {
        (sentence, sentence.split(" ").length)
      })

      count.collect().foreach(point => {
        println(point._1, point._2)
      })

    } finally {
      spark.stop()
    }
  }



}
