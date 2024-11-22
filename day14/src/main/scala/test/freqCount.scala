package test
import org.apache.spark.sql.SparkSession

object freqCount {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Freq Count").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    def sumByValues(a:Int,b:Int): Int = {
      a + b
    }
    try {
      val data = Seq("Hi there", "Hello", "Happy")

      val dataRdd =  sprk_ctx.parallelize(data)

      val process = dataRdd.flatMap(_.toCharArray)

      val flatMapRdd = process.map(a => (a,1))

      val countLet = flatMapRdd.reduceByKey(sumByValues)

      countLet.collect().foreach(point => {
        println(point)
      })

    } finally {
      spark.stop()
    }
  }

}
