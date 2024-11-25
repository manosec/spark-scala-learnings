import org.apache.spark.sql.SparkSession
import scala.util.Random

object experimentPartition_5 {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Experiment Partition")
      .master("local[*]")
      .getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {

      val dataCsv = sprk_ctx.textFile("src/main/data/large_dataset.csv")

      val partitionSizes = List(2, 4, 8)

      partitionSizes.foreach { numPartitions =>
        println(s"Processing with $numPartitions partitions")

        // Repartition the RDD
        val partitionedRDD  = dataCsv.repartition(numPartitions)

        // Action
        val rowCount = partitionedRDD.count()
        println(s"Number of rows: $rowCount")

        // Transformation
        val sortedRDD = partitionedRDD.sortBy(line => line)

        val outputPath = s"src/main/data/experimentPartition_5/data/partition_$numPartitions"
        sortedRDD.saveAsTextFile(outputPath)

        println(s"Finished processing for $numPartitions partitions. Check Spark UI for performance metrics.")
      }

    } finally {
      Thread.currentThread().join()
      spark.stop()
    }
  }

}
