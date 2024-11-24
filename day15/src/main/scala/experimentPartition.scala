import org.apache.spark.sql.SparkSession
import scala.util.Random

object experimentPartition {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Experiment Partition")
      .master("local[*]")
      .getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val numRows = 1000000
      val data = (1 to numRows).map(_ => Random.nextInt(10000))

      val partitionSizes = List(2, 4, 8)

      val rdd = spark.sparkContext.parallelize(data)

      partitionSizes.foreach { numPartitions =>
        println(s"Processing with $numPartitions partitions")

        // Repartition the RDD
        val partitionedRDD  = rdd.repartition(numPartitions)

        // Action
        val rowCount = partitionedRDD.count()
        println(s"Number of rows: $rowCount")

        // Transformation
        val sortedRDD = partitionedRDD.sortBy(line => line)

        val outputPath = s"src/main/data/partition_$numPartitions"
        sortedRDD.saveAsTextFile(outputPath)

        println(s"Finished processing for $numPartitions partitions. Check Spark UI for performance metrics.")
      }

    } finally {
      Thread.currentThread().join()
      spark.stop()
    }
  }

}
