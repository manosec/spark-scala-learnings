import org.apache.spark.sql.SparkSession

object narrowAndWideTransform_2 {
  def main(args: Array[String]): Unit = {
    var spark = SparkSession.builder().appName("Narrow and Wide Transformation").master("local[1]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try{
      val data = 1 to 1000

      val sparkData = sprk_ctx.parallelize((data))

      val mappedData = sparkData.map(points => points * 2)
      val filteresData = sparkData.filter(_ % 2 != 0)

      //Wide Transformation
      val wideData = sparkData.map(point => {
        (point * 5, point)
      })

      // Explore what is the logic here with recudeByKey method
      val recucedWideData = wideData.reduceByKey(_ + _)

      mappedData.saveAsTextFile("src/main/data/narrowWideMapped_N.txt")
      filteresData.saveAsTextFile("src/main/data/narrowWideFiltered_N.txt")
      recucedWideData.saveAsTextFile("src/main/data/narrowWideReduces_W.txt")



    } finally {
      Thread.currentThread().join()
      sprk_ctx.stop()
    }
  }
}
