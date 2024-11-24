import org.apache.spark.sql.SparkSession
import java.io.Reader

object rddAndParition {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("RDD and Parition").master("local[5]").getOrCreate()

    val sprk_ctx = spark.sparkContext
    val filePath = "src/main/data/spark_data.txt"

    val data = 1 to 10000
    try{

//      val dataLoad = sprk_ctx.textFile(filePath)
      val dataLoad = sprk_ctx.parallelize(data)

      val initPartition = dataLoad.getNumPartitions

      println(s"The Initial Default parition is ${initPartition}")

      val reParition = dataLoad.repartition(4)

      println(s"The partition now is ${reParition.getNumPartitions}")

      val coalesceData = reParition.coalesce(3)
      println(s"The Coalesce partition is ${coalesceData.getNumPartitions}")


      coalesceData.mapPartitionsWithIndex((index, iterator) => {
        val records = iterator.take(5).mkString("\n")
        println("in map function")
        if (records.nonEmpty){
          println(s"This first 5 record the parition ${index} is ${records}")
        }
       iterator
      })
//      coalesceData.foreachPartition(partition => {
//        println(s"Partition: ${partition.toString().take(50)}...") // Print first 50 characters
//      })

    } finally {
//      Thread.currentThread().join()
      spark.stop()
    }
  }
}
