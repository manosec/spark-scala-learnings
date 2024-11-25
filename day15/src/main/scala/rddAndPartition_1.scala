import org.apache.spark.sql.SparkSession
import java.io.Reader

object rddAndPartition_1 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("RDD and Parition").master("local[5]").getOrCreate()

    val sprk_ctx = spark.sparkContext
    val filePath = "src/main/data/spark_data.txt"

    val data = 1 to 10000
    try{

      val dataLoad = sprk_ctx.parallelize(data)


      val reParition = dataLoad.repartition(6)

      val mappedPartition = reParition.map(point => {
        point * 2
      })

      val coalesceData = mappedPartition.coalesce(3)

      coalesceData.collect().foreach(println)


    } finally {
      Thread.currentThread().join()
      spark.stop()
    }
  }
}
