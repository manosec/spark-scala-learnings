package test
import org.apache.spark.sql.SparkSession

object cartesianProd {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("Cartesian Product").master("local[*]").getOrCreate()

    val sprk_ctx = spark.sparkContext

    try {
      val data1 = List(1,2,3,4,5)
      val data2 = List(6,7,8,9,10)

      val data1Rdd = sprk_ctx.parallelize(data1)
      val data2Rdd = sprk_ctx.parallelize(data2)

      val cartesianStruct = data1Rdd.cartesian(data2Rdd)

      val finalProduct = cartesianStruct.map(a => {
        a._1 * a._2
      })

      finalProduct.collect().foreach(a => {
        println(a)
      })


    } finally {

    }
  }
}
