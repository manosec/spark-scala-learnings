import org.apache.spark.sql.SparkSession
import org.apache.spark.TaskContext

object manualTaskDebug {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Manual Task Allocation").master("local[1]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try{
      val data = List(1,2,3,4,5,6,7,8,9,10)

      val data_rdd = sprk_ctx.parallelize(data, numSlices = 5)

      val processPart = data_rdd.map { dataPoint =>
        val partitionId = TaskContext.getPartitionId()
        val taskId = TaskContext.get.taskAttemptId()
        (dataPoint, partitionId, taskId)
      }

      val output = processPart.filter(a => a._2 > 5)

      processPart.collect().foreach(data => {
        println(s"The data point ${data._1} is in ${data._2}th parition and processed by ${data._3} task")
      })

      output.collect().foreach(data => {
        println(s"The data point ${data._1} is in ${data._2}th parition and processed by ${data._3} task")
      })

      Thread.currentThread().join()
    } finally {
//      spark.stop()
    }
  }
}